package com.weatnet;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class Client {

    public static String myToken;

    public static void main(String[] args) {

        try {
            Scanner scanner = new Scanner(System.in);
            Socket socket = new Socket("localhost", 4444);
            InputStream is = socket.getInputStream();
            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            OutputStream os = socket.getOutputStream();

            /**
             * Iniating streams and sockets.
             * */
            int lengthOfArray=0;
            boolean auth=false;
            writeToServer(0,0,os,scanner);
            /**
             * We are sending our username with phase:0 type:0.
             * */
            /*try{
                byte[] applicationHeader=new byte[6];
                is.read(applicationHeader,0,6);

                int length= findLengthOfString(applicationHeader);
                String msg2= readFollowingByte(is,length);
                System.out.println("Server message: "+msg2);
                /**
                 * Reading server's message. This can be error or Authentication questions.
                 * *
            }catch(IOException e){
                e.printStackTrace();
            }*/
            while (true) {

                try{

                    byte[] recieved=new byte[6];
                    is.read(recieved,0,6);
                    lengthOfArray= findLengthOfString(recieved);

                    if(recieved[1]==(byte)3){
                        myToken= readFollowingByte(is,lengthOfArray);
                        System.out.println("Authentication is done, Token has received");
                        //System.out.println(myToken);
                        auth=true;
                        break;
                    }else{
                        auth=false;
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }

                String sk2= readFollowingByte(is,lengthOfArray);
                /***
                 * Reading server's message which can be Success message, another question or fail message.
                 * */
                System.out.println(sk2);

                if (sk2.contains("quit")) {
                    /**
                     * Closing the connection if server writes quit.
                     * */
                    System.out.println("Closing this connection: " + socket);
                    socket.close();
                    System.out.println("Connection is closed");
                    break;
                }
                /**
                 * answers server's message which is probably another autentication question.
                 * */
                String toSend = scanner.nextLine();//We read
                byte[] toSendByte=new byte[toSend.length()+6];
                toSendByte= stringToByteArray(0,0,toSend,toSendByte);
                os.write(toSendByte);
                os.flush();

                byte[] applicationHeader2=new byte[6];
                is.read(applicationHeader2,0,6);
                int  lengthOfArray2= findLengthOfString(applicationHeader2);
                String response= readFollowingByte(is,lengthOfArray2);
                System.out.println(response);
                /**
                 * Reads server's response to our answers.
                 * */

            }
            if(auth){
                /**
                 * Authentication is done. Data port will be here.
                 * */
                // ClientDataSocket dataWorker = new ClientDataSocket(4445);
                System.out.println("auth connection will take place");
                handleWeather(socket);
            }

            scanner.close();
            dataInputStream.close();
            os.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleWeather(Socket socket) {
        int dataPort = -1;
        try {
            Scanner scanner = new Scanner(System.in);
            Socket dataSocket = null;

            DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());

            System.out.println(dataInputStream.readUTF());

            while (true) {
                String receivedd = "";
                if (dataInputStream.available() != 0) {
                    receivedd = dataInputStream.readUTF();
                    System.out.println(receivedd);
                }
                String toSend0 = scanner.nextLine();
                String toSend = myToken+" "+toSend0;
                dataOutputStream.writeUTF(toSend);
                //String answer22=dataInputStream.readUTF();
                //System.out.println(answer22);
                //hocam


                if (toSend.contains("quit")) {
                    System.out.println("Closing this connection: " + socket);
                    socket.close();
                    System.out.println("Connection is closed");
                    break;
                }  else if (receivedd.split(" ")[0].equalsIgnoreCase("dataport")) {
                    dataPort = Integer.parseInt(receivedd.split(" ")[1]);
                    //System.out.println("dataport--> "+dataPort);


                } else if (toSend.split(" ")[1].equalsIgnoreCase("get")) {

                    String type = toSend.split(" ")[3];
                    if (!type.equals("clouds") && !type.equals("pressure") && !type.equals("wind") && !type.equals("temp")) {
                        int dataPrt = dataInputStream.readInt();
                        System.out.println(dataPrt);
                        dataSocket = new Socket("localhost",dataPrt);
                        System.out.println("requested");
                        String receivedWeather = new DataInputStream(dataSocket.getInputStream()).readUTF();
                        int fileHash = receivedWeather.hashCode();
                        System.out.println(receivedWeather);
                        //System.out.println(receivedHash);
                        int hashCommand = dataInputStream.readInt();
                        if (hashCommand == fileHash) {
                            System.out.println("Hash value is correct! -> " + hashCommand);
                            dataOutputStream.writeUTF("OK");
                        } else {
                            dataOutputStream.writeUTF("Resend");
                        }
                    } else if (!type.equals("current") && !type.equals("daily") && !type.equals("hourly") && !type.equals("minutely") && !type.equals("historical")) {
                        System.out.println("Image");
                        int dataPrt = dataInputStream.readInt();
                        System.out.println(dataPrt);
                        dataSocket = new Socket("localhost",dataPrt);
                        System.out.println("requested image");
                        BufferedImage bufferedImage = ImageIO.read(dataSocket.getInputStream());

                        //JFrame frame = new JFrame();
                        //frame.setTitle("Requested Weather Image");
                        //frame.setSize(250, 250);
                        //Container contentPane = frame.getContentPane();
                        //JLabel sentenceLabel= new JLabel(new ImageIcon(bufferedImage));
                        //contentPane.add(sentenceLabel);
                        //frame.show();

                        System.out.println("Image Received");
                        int fileHash = bufferedImage.hashCode();
                        System.out.println(bufferedImage);
                        //System.out.println(receivedHash);
                        int hashCommand = dataInputStream.readInt();
                        if (hashCommand == fileHash) {
                            System.out.println("Hash value is correct! -> " + hashCommand);
                            dataOutputStream.writeUTF("OK");
                        } else {
                            dataOutputStream.writeUTF("Resend");
                        }
                        //String receivedWeather = new DataInputStream(dataSocket.getInputStream()).readUTF();
                        //int hashCommand = dataInputStream.readInt();
                    }

                        /*
                    }else {
                        dataOutputStream.writeUTF("Wrong token, try your request again"); // NOT SURE ???????
                        //sout ?
                        continue;
                    }
                    */


                }
                //


                try {
                    String received = dataInputStream.readUTF();
                    System.out.println(received);
                } catch (EOFException e) {
                    System.err.println("Timeout occured. Closing connection"); // DID YOU CLOSE THE CMD TOO ????
                    socket.close();
                    break;
                }

            }
            scanner.close();
            if (dataSocket != null) dataSocket.close();
            dataInputStream.close();
            dataOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeToServer(int a, int b, OutputStream os, Scanner scanner){
        try{
            String toSend = scanner.nextLine();//We read
            byte[] toSendByte=new byte[toSend.length()+6];
            toSendByte= stringToByteArray((byte)a,(byte)b,toSend,toSendByte);
            os.write(toSendByte);
            os.flush();
        }catch (IOException e){
            e.printStackTrace();
        }

    }
    private static String byteArrayToString(byte[] byteArrayWoAppHeader, int a){
        char oneCharOfString='a';
        /**
         * We are decoding byteArray. We know the lengthOfTheMessage with the help of findLLength Method.
         * */
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<a;i++){
            oneCharOfString=(char)byteArrayWoAppHeader[i];
            sb.append(oneCharOfString);
        }
        String finalString=sb.toString();
        return finalString;
    }
    private static String readFollowingByte(InputStream is, int length){
        String messageString="error";
        byte controlByte=1;
        try{


            byte[] stepArray=new byte[length];
            is.read(stepArray,0,length);
            messageString= byteArrayToString(stepArray,length);

        }catch (IOException e){
            e.printStackTrace();
        }
        return messageString;
    }
    private static byte[] intTo4Bytes(final int i ) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(i);
        /**
         * This method allow us to convert our integer value to 4 byte and saves it to a byte array.
         * */
        return bb.array();
    }
    private static int findLengthOfString(byte[] intBytes){
        /***
         * Find the length of the following string from an application header
         * */
        byte[] lengthArray=new byte[4];
        for(int i=2;i<6;i++){
            lengthArray[i-2]=intBytes[i];
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(lengthArray);
        return byteBuffer.getInt();
    }
    public static byte[] stringToByteArray(int a,int b,String message, byte[] ByteList){

        /**
         * This method converts regular string to Byte array with an application header.
         * */
        char[] myCharArray= message.toCharArray();
        byte ourByte=(byte)a;
        byte ourByte2=(byte)b;

        int length=message.length();
        ByteList[0]=(ourByte);//initiation of first byte which is always zero for client
        ByteList[1]=(ourByte2);//there is no authentication take place yet

        byte[] ByteList2= intTo4Bytes(length);

        for (int i=2;i<(6);i++){

            ByteList[i]=ByteList2[i-2];
        }
        for (int i=6;i<(message.length()+6);i++){

            Byte k=(byte) myCharArray[i-6];

            ByteList[i]=k;

        }
        // System.out.println(ByteList);
        return ByteList;
    }

}
