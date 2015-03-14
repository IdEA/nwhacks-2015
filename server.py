#%!/usr/bin/env python3
import re, os
from socket import *

BYTES_TO_RECEIVE = 1024
SCRIPT_LOCATION = os.path.realpath(os.path.join(os.getcwd(), os.path.dirname(__file__)))
SERVER_PORT = 12000

serverSocket = socket(AF_INET, SOCK_STREAM)
# Reuse local addresses
serverSocket.setsockopt(SOL_SOCKET, SO_REUSEADDR, 1)
serverSocket.bind(('0.0.0.0', SERVER_PORT))
# Maximum 10 clients
serverSocket.listen(10)

# Form the HTTP header response to be sent to the client
def formHeaderResponse():
    response = ("HTTP/1.1 200 TRUE\r\n\r\n")
    print("Sending header response 200: " + repr(response))
    return response.encode('utf-8')

# Form the binary response to be sent to the client
def formBinaryResponse(bfLength, bfName):
    # HTTP protocol uses CRLF type endlines
    response = ("HTTP/1.1 200 OK\r\n"
                "Accept-Ranges: bytes\r\n"
                "Keep-Alive: timeout=10, max=100\r\n"
                "Connection: Keep-Alive\r\n"
                # Set content-length, content-type, disposition to faciliate binary download
                "Content-Length: " + str(bfLength) + "\r\n"
                "Content-Type: application/octet-stream\r\n"
                # HTTP protocol expects two endlines as header termination
                "Content-Disposition: attachment; filename=" + bfName + "\r\n\r\n")
    print("Sending content-header response 200: " + repr(response))
    return response.encode('utf-8')

# Form the HTTP 404 response to be sent to the client
def form404Response(rf, isGetRequest):
    html = ("<center>Error 404: File not found!<br>"
            "You have requested for a non existing file: <b>" + rf + "</b><br><br>"
            "Please try another file</center>")
    response = ("HTTP/1.1 404 Not Found\r\n"
                "Keep-Alive: timeout=10, max=100\r\n"
                "Content-Length: " + str(len(html)) + "\r\n"
                "Content-Type: text/html\r\n\r\n")
    if isGetRequest:
        print("Sending content-header response 404: " + repr(response + html))
        return (response + html).encode('utf-8')
    else:
        print("Sending header response 404: " + repr(response))
        return response.encode('utf-8')

# Form the HTTP homepage response to be sent to the client
def formHomePageResponse():
    html = ("<center><b>Welcome!</b><br>"
            "You have reached the idEA nwhacks cache server<br><br>"
            "Enjoy your stay!</center>")
    response = ("HTTP/1.1 200 OK\r\n"
                "Keep-Alive: timeout=10, max=100\r\n"
                "Content-Length: " + str(len(html)) + "\r\n"
                "Content-Type: text/html\r\n\r\n")
    print("Sending content-header homepage response: " + repr(response + html))
    return (response + html).encode('utf-8')

def formAPIResponse(__resp):
    return __resp.encode('utf-8')

def formACK():
    return 'x'.encode('utf-8')

print("Server is listening...")

inQ = []

try:
	# Main listen loop
    while True:
        connectionSocket, addr = serverSocket.accept()
        request = connectionSocket.recv(BYTES_TO_RECEIVE).decode('utf-8')
        if not request:
            continue

        # Get the request type and file
        try:
            requestType = request.split()[0]
            requestedFile = request.split()[1]
            requestStr = requestedFile.split('/')[1:]
            print(len(requestStr))
            if len(requestStr) >= 2:
                apiType = requestStr[0]
                apiVal = requestStr[1]
                if apiType == 'put':
                    print("PUtting: " + apiVal)
                    inQ.append(apiVal)
                    connectionSocket.send(formACK());
                elif apiType == 'get':
                    respCSV = ""
                    while len(inQ) > 0:
                        itm = inQ.pop(0)
                        respCSV += itm #+ ","
                        print("Popped: " + itm)
                    connectionSocket.send(formAPIResponse(respCSV))
                    print("Sent: " + respCSV)
            # Only handle GET and HEAD request types
            if requestType != 'GET' and requestType != 'HEAD':
                raise Exception;
            print("\nIncoming request: " + repr(request))
        except Exception as ff:
            print(ff)
            print("Malformed request; ignoring..")
            continue

        # Client requests homepage, continue
        if requestedFile == "/":
            connectionSocket.send(formHomePageResponse())
            continue

        # skip
        connectionSocket.close();
        continue

        print("Requested file: " + repr(requestedFile))
        try:
            # Open file in read-only, binary mode, trim /
            binaryFile = open(os.path.join(SCRIPT_LOCATION, requestedFile[1:]), 'rb')
            print(" FOUND AT " + SCRIPT_LOCATION)
			# If this is a GET request, try to send the contents of the file
            if requestType == 'GET':
                data = binaryFile.read().decode('utf-8')
                connectionSocket.send(formBinaryResponse(len(data), requestedFile))
                for i in range(0, len(data)):
                    connectionSocket.sendall(data[i].encode('utf-8'))
                    print("Sending byte " + str(i + 1) + ": " + repr(data[i]))

                contentLenStr = str(len(data))
                print("Finished sending " + contentLenStr + "/" + contentLenStr + " bytes of ." + requestedFile + " to client")
            else:
                # Otherwise, send the header only
                connectionSocket.send(formHeaderResponse())
            binaryFile.close()
        except ConnectionError as e:
            # Ignore if client crashes
            print(str(e) + ": Client probably exploded, RIP")
        except IOError:
            # The file could not be found. Send 404 response
            print(" NOT FOUND at " + SCRIPT_LOCATION)
            connectionSocket.send(form404Response(requestedFile, requestType == 'GET'))
        finally:
            connectionSocket.close()
except KeyboardInterrupt:
    print("\n^C Detected: Terminating gracefully")
finally:
    print("Server socket closed")
    serverSocket.close()
