package tcp.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import tcp.model.MessageJson;
import tcp.model.MessageJsonExtended;
import tcp.model.MessageType;
import utils.Constants;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;
import java.util.TreeMap;

public class ServerAPPThread extends Thread {
    private final Socket clientSocket;
    private PrintWriter writer;
    private BufferedReader in;

    public ServerAPPThread(final Socket clientSocket) {
        this.clientSocket = clientSocket;
    }


    /*

        From: B
        To: A
        Name: galileo.jpg
        Data:
        C926A21889B029B502E2FA3977B9B79DD94240852CE748A4DACBA0C56B9594260017A4D589BA48AD362746596A40
        4E201329A6A9500995973F4734BDA9CF1EE0D024C91C95200082462048C450821880043114D201000
        Frag: 4
        Size: 4553
        EOF

        -- Propuesta --
        {
            "from": "B",
            "type": "HELLO"
            "to": "A",
            "name": "galileo.jpg",
            "data":
            "TG9yZW0gaXBzdW0gZG9sb3Igc2l0IGFtZXQsIGNvbnNlY3RldHVyIG           ----> size de 1,460
             FkaXBpc2NpbmcgZWxpdC4gRXRpYW0gZGljdHVtLCBudW5jIGV1IHN1c2NpcGl0IHNlbXBlciwgbWF1cmlzIG",
            "frag": 4,
            "size": 4553
        }

        -- Hello
        {
            "from": "B",
            "type": "HELLO",
        }

        -- Welcome
        {
            "from": "A",
            "type": "WELCOME",
        }

        -- Keep Alive
        {
            "from": "B",
            "type": "KEEP_ALIVE",
        }

        -- Distance Vector
        {
            "from": "B",
            "type": "DV",
            "vectors": [
                {
                    "node": "C",
                    "cost": 5
                },
                {
                    "node": "B",
                    "cost": 3
                }
            ]
        }
     */


    public void run() {
        try {
            while (true) {
                writer = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                int length = 0;
                TreeMap<Integer, byte[]> chunks = new TreeMap<>();

                //Esta configuracion es importante para que funcione correctamente
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                while (true) {
                    String clientMessage = in.readLine();

                    //Aca obtenemos el mensaje enviado desde el cliente en formato JSON
                    //y se transforma a un objeto
                    MessageJson messageJson = objectMapper.readValue(clientMessage, MessageJson.class);
                    String fileName = messageJson.getFileName();
                    int fileLength = messageJson.getTotalLength();
                    String chunkObject = messageJson.getChunk();

                    //De esta forma podemos recibir diferentes tipos de mensajes con
                    //diferentes propiedades
                    if (messageJson.getType() == MessageType.EXTENDED) {
                        MessageJsonExtended messageJsonExtended = objectMapper.readValue(clientMessage,
                                MessageJsonExtended.class);

                        System.out.printf("--> Message extended received, extended property %s %n",
                                messageJsonExtended.getExtendedProperty());
                    }

                    String chunk = chunkObject.substring(4);
                    String position = chunkObject.substring(0, 4);
                    Integer chunkPosition = Integer.parseInt(position, 16);
                    int chunkLength = chunk.length();
                    length = length + chunkLength;

                    System.out.printf("Receiving chunk number: %d, with length: %d, chunk: %s %n",
                            chunkPosition, chunkLength, chunk);

                    //Base 64 Decode, aca se toma el chunk enviado desde el cliente
                    //y se transforma a bytes para armar el archivo
                    chunks.put(chunkPosition, Base64.getDecoder().decode(chunk));

                    if (length == fileLength) {
                        String outputFilename = Constants.SERVER_OUTPUT_PATH + fileName;
                        try (OutputStream outputStream = new FileOutputStream(outputFilename)) {
                            for (final byte[] value : chunks.values()) {
                                outputStream.write(value);
                            }
                        }

                        System.out.println("File name: " + fileName);
                        System.out.println("File length: " + fileLength);
                        System.out.println("Total chunks: " + chunks.size());
                        System.out.println("File created successfully - " + outputFilename);

                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
        }
    }

    public void stopConnection() throws IOException {
        in.close();
        writer.close();
        clientSocket.close();
    }

}