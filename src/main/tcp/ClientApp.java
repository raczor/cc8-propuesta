package tcp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import tcp.model.FileInfo;
import tcp.model.MessageJson;
import tcp.model.MessageJsonExtended;
import tcp.model.MessageType;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class ClientApp {
    static Socket clientSocket;
    static PrintWriter writer;
    static BufferedReader in;

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);

            clientSocket = new Socket("127.0.0.1", 8090);
            writer = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            while (true) {
                System.out.print("Enter the file name: ");

                final String message = scanner.nextLine();

                sendMessage(message);
            }
        } catch (Exception e) {
            System.out.println("Error");
            e.printStackTrace();
        }
    }

    public static void sendMessage(final String message) throws Exception {
        final URL resource = ClientApp.class.getClassLoader().getResource("files/" + message);
        if (resource == null) {
            return;
        }

        final FileInfo fileInfo = generateChunks(resource);
        final List<String> chunks = fileInfo.getChunks();
        final int totalLength = fileInfo.getTotalLength();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Random random = new Random();

        while (!chunks.isEmpty()) {
            final String chunk = chunks.remove(0);

            //Aqui creamos el objeto que contendra toda la informacion

            boolean isNormalMessage = random.nextBoolean();

            MessageJson messageJson;
            if (isNormalMessage) {
                messageJson = new MessageJson();

                messageJson.setType(MessageType.NORMAL);
            } else {
                MessageJsonExtended messageJsonExtended = new MessageJsonExtended();

                messageJsonExtended.setType(MessageType.EXTENDED);
                messageJsonExtended.setExtendedProperty(random.nextInt() + "");

                System.out.println("--> Sending extended property: " + messageJsonExtended.getExtendedProperty());

                messageJson = messageJsonExtended;
            }

            messageJson.setFileName(message);
            messageJson.setTotalLength(totalLength);
            messageJson.setChunk(chunk);

            //En esta parte transformamos el objeto a un Json string
            writer.println(objectMapper.writeValueAsString(messageJson));
        }
    }

    public static FileInfo generateChunks(URL resource) throws Exception {
        FileInfo fileInfo = new FileInfo();

        byte[] buffer = new byte[384];

        int lenBytes;
        int chunksQuantity = 0;

        InputStream stream = new FileInputStream(resource.toURI().getPath());
        while ((lenBytes = stream.read(buffer)) > 0) {
            byte[] aux = new byte[lenBytes];
            System.arraycopy(buffer, 0, aux, 0, lenBytes);

            //En esta parte tomamos los bytes del archivo y lo transformamos a Base 64
            String base64EncodedString = Base64.getEncoder().encodeToString(aux);
            String base16EncodedIndex = String.format("%04x", ++chunksQuantity);
            fileInfo.addTotalLength(base64EncodedString.length());

            fileInfo.addChunk(base16EncodedIndex + base64EncodedString);
        }

        return fileInfo;
    }

}