package com.weatnet;

import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class ServerWorker extends Thread {
    OutputStream outputStream;
    DataInputStream dataInputStream;
    DataOutputStream dataOutputStream;
    Socket socket;
    int TIMEOUT_SEC = 100;
    //String[] users = {"admin", "ata", "aycan","ali","emre"};
    String[] questions = {"favorite color?", "favorite author?", "born place?","bf?", "course goal?"};
    String[] answers1 = {"blue", "adams", "machine","ata","A+"};
    String[] answers2 = {"green", "dostoyevski", "istanbul","aycan","A"};
    String[] answers3 = {"pink", "tolstoy", "izmir","ali","A-"};
    String[] answers4 = {"red", "papini", "antep","emre","B+"};
    String[] answers5 = {"white", "marquez","adana","admin","B"};

    int questionCounter;
    String usernameOfTheClient;

    public static ArrayList<String> tokensList = new ArrayList<String>();
    static ArrayList<String> usernameList = new ArrayList<String>();
    static{
// Username list that is allowed to connect our port
        usernameList.add("admin");
        usernameList.add("ata");
        usernameList.add("aycan");
        usernameList.add("ali");
        usernameList.add("emre");
    }
    HashMap<String,String> IpToToken = new HashMap<String,String>();
    HashMap<String,String> UsernameToIp = new HashMap<String,String>();
    //token = ("username"+"random-number").hash()
    //IP: IP+port number

    //when username entered, put username into UsernameToIp with "1", this ensures unique user
    //(all rest will be "0")
    //if authenticated, override the username hashmap key to give value -> IP+port number
    //also, with that



    public ServerWorker(Socket socket) {
        this.socket = socket;



    }

    @Override
    public void run() {

        String currentUser="";// We have the current user that is in the code.
        int lengthOfArray=0;
        boolean authorizationCheckB=true;//this method decides whether authentication taking place its initially true
        try{
            OutputStream os = socket.getOutputStream();
            InputStream is = socket.getInputStream();
            /**
             Application headers will be taken from client to control their phase and type
             */
            byte[] applicationHeader=new byte[6];
            is.read(applicationHeader,0,6);

            if(applicationHeader[0]==(byte)0){
                /**
                 * Correct Phase for Authentication
                 * */
                if(applicationHeader[1]==(byte)0){
                    /**
                     * Correct Type for Authentication
                     * */
                    lengthOfArray=findLengthOfString(applicationHeader);
                    usernameOfTheClient=stringReaderFromByteArray(is,lengthOfArray);
                    System.out.println(usernameOfTheClient);
                    /**
                     * Answer of the Client is read.
                     * */
                    if(usernameList.contains(usernameOfTheClient)){
                        // we need to implement hashmap that contains usernames to control.

                        /////////
                        String[] answerSelector ;
                        if (usernameOfTheClient.equals("admin")) {
                            answerSelector=answers1;
                        } else if (usernameOfTheClient.equals("ata")) {
                            answerSelector=answers2;
                        } else if (usernameOfTheClient.equals("aycan")) {
                            answerSelector=answers3;
                        } else if (usernameOfTheClient.equals("ali")) {
                            answerSelector=answers4;
                        } else { //emre
                            answerSelector=answers5;
                        }

                        Random rand = new Random();
                        int amount = 1 + rand.nextInt(questions.length); //upper limit exclusive
                        List<String> questionList = new ArrayList(Arrays.asList(questions));
                        //List<String> answerList = new ArrayList(Arrays.asList(answers));
                        ArrayList<String> currentQuestions = new ArrayList<>();
                        ArrayList<String> currentAnswers = new ArrayList<>();
                        Collections.shuffle(questionList);
                        for (int i = 0; i < amount; i++) {
                            currentQuestions.add(questionList.get(i));
                        }


                        for (int i = 0; i < currentQuestions.size(); i++) {
                            currentAnswers.add( answerSelector[Arrays.asList(questions).indexOf(currentQuestions.get(i))] );
                        }
                        /////////


                        currentUser=usernameOfTheClient;//We know the current User
                        System.out.println(socket.getLocalAddress()+currentUser+"Enters to authentication.");



                      /* try{
                           String error1="Correct Username. Authentication is starting.";
                           byte[] error1Byte=new byte[error1.length()+6];
                           error1Byte=StringToByteArray(0,3,error1,error1Byte);
                           os.write(error1Byte);
                           os.flush();

                       }catch (IOException e){
                           e.printStackTrace();
                       }*/
                        authorizationCheckB=authorizationCheck(currentUser, currentQuestions, currentAnswers);


                        try{
                            FileWriter fw=new FileWriter("q&a.txt",true);
                            fw.write("user--> "+usernameOfTheClient+"\n");
                            for (int i=0; i<questionCounter;i++) {
                                fw.write(currentQuestions.get(i)+"\n");
                                fw.write(currentAnswers.get(i)+"\n");

                            }
                            fw.write("\n");
                            fw.close();
                        }catch(Exception e) {
                            System.out.println(e);
                        }

                        if(authorizationCheckB){

                            //start data
                            //DataServer dataserver = new DataServer(4445);
                            //dataserver.start();

                            /**
                             * Data port will be put here.
                             * */
                            System.out.println("DATA SERVER IS STARTING");
                            handleRequests();
                        }

                    }  else {
                        /**
                         * Invalid Username. Connection will be terminated here.
                         * */
                        String error1="Invalid Username. Connection terminated.";
                        byte[] error1Byte=new byte[error1.length()+6];
                        error1Byte=StringToByteArray(0,2,error1,error1Byte);
                        os.write(error1Byte);
                        os.close();
                        is.close();
                        socket.close();
                        System.out.println("Socket is closed due to invalid username type");
                    }
                } else {
                    /**
                     * We are in authentication phase. That is why, Client's type should be zero.
                     * */
                    String error1="Invalid Type";
                    byte[] error1Byte=new byte[error1.length()+6];
                    error1Byte=StringToByteArray(0,2,error1,error1Byte);
                    os.write(error1Byte);

                    os.close();
                    is.close();
                    socket.close();

                    System.out.println("Socket is closed due to invalid phase type");

                }

            } else {

                String error1="Invalid Phase. Connection denied";
                byte[] error1Byte=new byte[error1.length()+6];
                error1Byte=StringToByteArray(0,2,error1,error1Byte);
                os.write(error1Byte);
                os.flush();
                os.close();
                is.close();
                socket.close();
                System.out.println("Socket is closed due to invalid phase type");

                //data function
            }


        }catch(IOException e){
            e.printStackTrace();
        }

    }




    private boolean authorizationCheck(String currentUser, ArrayList currQ, ArrayList currA ) {
        /**
         * Question ans Answers for the below Authorization process. Every question have a unique answer.
         *
         * Question number is random for every connection.
         * */



        try {
            socket.setSoTimeout(TIMEOUT_SEC*1000);
        } catch ( SocketException e) {
            e.printStackTrace();
        }


        int lengthOfArray= 0;
        questionCounter=0;
        //boolean controlVariable=false;//This control variable is controling whether autentiaton taken place
        //Random rand=new Random();
        //int numberOfQuestion=rand.nextInt(questions.length)+1;
        //System.out.println(numberOfQuestion+" questions will be asked");

        int numberOfQuestion = currQ.size();

        /**
         * Server knows have many questions will be asked. Clients do not know.
         * */
        while (true) {
            try{
                OutputStream outputStreamOfServer = socket.getOutputStream();
                InputStream is = socket.getInputStream();
                /**
                 * Initializing streams again for the below write and reads
                 *
                 * */
                if(questionCounter==numberOfQuestion){
                    /**
                     * Tokens will be sent in here.
                     * Client can access dataport with this token which is random for every client.
                     * */

                    String currToken = giveToken(currentUser);
                    //token = ("username"+"random-number").hash()
                    //IP: IP+port number


                    InetAddress IP = (Inet4Address) (((InetSocketAddress) (socket.getRemoteSocketAddress()) ).getAddress());
                    /**
                     * getRemoteSocketAddress-> returns a protocol-dependent subclass.
                     * For internet protocols (like TCP), we cast it to an InetSocketAddress (internet)
                     * getAddress is a method of InetSocketAddress class, which gets internet address (hence the name)
                     * Then, we can cast that to an Inet4Address or Inet6Address depending on the address type
                     **/

                    String IPstr = IP.toString();
                    String PORTstr = String.valueOf(socket.getLocalPort());


                    IpToToken.put(IPstr+PORTstr,currToken);



                    byte[]  token2=new byte[currToken.length()+6];
                    token2=StringToByteArray(1,3,currToken,token2);
                    outputStreamOfServer.write(token2);
                    outputStreamOfServer.flush();

                    //controlVariable=true;
                    //break;

                    return true;

                }
                String temp = String.valueOf(currQ.get(questionCounter));

                //byte[]  questionByteArray=new byte[questions[questionCounter].length()+6];

                byte[]  questionByteArray=new byte[temp.length()+6];

                //questionCounter is just a counter for questions(questions will increase)
                //questionByteArray=StringToByteArray(0,1,questions[questionCounter],questionByteArray);

                questionByteArray=StringToByteArray(0,1,temp,questionByteArray);


                //we know there is no auth
                outputStreamOfServer.write(questionByteArray);
                outputStreamOfServer.flush();
                System.out.println("Question asked");
                /**
                 * Question is asked at the above.
                 * */


                byte[] applicationHeader=new byte[6];
                try {
                    is.read(applicationHeader,0,6);
                } catch (InterruptedIOException e) {
                    System.err.println("Timeout occured. Disconnecting client " + socket);
                    socket.close();
                    break;
                }
                if(applicationHeader[0]!=0){
                    String error1="Invalid Phase. Connection denied";
                    byte[] error1Byte=new byte[error1.length()+6];
                    error1Byte=StringToByteArray(0,2,error1,error1Byte);
                    outputStreamOfServer.write(error1Byte);
                    outputStreamOfServer.flush();
                    outputStreamOfServer.close();
                    is.close();
                    socket.close();
                    System.out.println("Socket is closed due to invalid phase");
                }
                /**
                 *
                 * We are checking the application header's phase and type
                 *
                 * Client is in the authentication so both must be zero.
                 * */
                if(applicationHeader[1]!=0){
                    String error1="Invalid Type. Connection denied";
                    byte[] error1Byte=new byte[error1.length()+6];
                    error1Byte=StringToByteArray(0,2,error1,error1Byte);
                    outputStreamOfServer.write(error1Byte);
                    outputStreamOfServer.flush();
                    outputStreamOfServer.close();
                    is.close();
                    socket.close();
                    System.out.println("Socket is closed due to invalid type");
                }
                /**
                 * Client has correct application headers
                 * */

                lengthOfArray=findLengthOfString(applicationHeader);
                String clientAnswer=stringReaderFromByteArray(is,lengthOfArray);
                System.out.println("Client"+ socket.getLocalPort()+" answered:"+clientAnswer);
                /**
                 * We are reading clients answer now.
                 *
                 * Code will evaluate its answer.
                 **/


                String temp2 = String.valueOf(currA.get(questionCounter));

                //if(clientAnswer.equalsIgnoreCase(answers[questionCounter])){
                if(clientAnswer.equalsIgnoreCase(temp2)){
                    System.out.println("Correct Answer");
                    String correctAns="Correct Answer";
                    byte[] correct1Byte=new byte[correctAns.length()+6];
                    correct1Byte=StringToByteArray(0,3,correctAns,correct1Byte);
                    outputStreamOfServer.write(correct1Byte);
                    outputStreamOfServer.flush();
                    /**
                     * Client answered correctly. We are writing both our and its console.
                     * */
                }else{
                    String error1="Invalid answer";
                    byte[] error1Byte=new byte[error1.length()+6];
                    error1Byte=StringToByteArray(0,2,error1,error1Byte);
                    outputStreamOfServer.write(error1Byte);
                    outputStreamOfServer.flush();
                    outputStreamOfServer.close();
                    is.close();
                    socket.close();
                    System.out.println("closed everything.");
                    break;
                    /**
                     * In case of a wrong answer, Server closing the connection immediately.We are informing the client why connection closed.
                     * */
                }
                questionCounter++;



                if(clientAnswer.contains("quit")){
                    /**
                     * Client can exit from the server by writing quit.
                     * */
                    outputStreamOfServer.close();
                    is.close();
                    socket.close();
                    System.out.println("closed everything.");
                    break;
                }

            }catch (IOException e){
                e.printStackTrace();
            }

        }

        return false;
    }


    private void handleRequests() throws IOException {
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());

        String received;
        socket.setSoTimeout(TIMEOUT_SEC*1000);
        Socket dataSocket = null;
        while (true) {
            try {
                try {
                    dataOutputStream.writeUTF("Enter your request: ");
                    dataOutputStream.flush();
                    received = dataInputStream.readUTF();
                } catch (InterruptedIOException e) {
                    System.err.println("Timeout occured. Disconnecting client " + socket);
                    socket.close();
                    if (dataSocket != null) dataSocket.close();
                    break;
                }

                if (received.contains("quit")) {
                    System.out.println("com.final_aeh.Client " + socket + " sends request to exit");
                    System.out.println("Closing the connection for " + socket);
                    socket.close();
                    break;
                } else if (received.split(" ")[1].equalsIgnoreCase("get")){

                    InetAddress thisIP = (Inet4Address) (((InetSocketAddress) (socket.getRemoteSocketAddress()) ).getAddress());
                    String thisIPstr = thisIP.toString();
                    String thisPORTstr = String.valueOf(socket.getLocalPort());

                    if (received.split(" ")[0].equals(IpToToken.get(thisIPstr+thisPORTstr))) {

                        //dataOutputStream.writeUTF("Correct token");
                        //hocam

                        dataOutputStream.writeInt(4445);
                        dataSocket = Main.getServerSocketData().accept();
                        DataSocketThread dataSocketThread = new DataSocketThread(dataSocket, received);
                        dataSocketThread.run();
                        int hash = dataSocketThread.getHashValue();
                        dataOutputStream.writeInt(hash);
                        String response = dataInputStream.readUTF();
                        if (!response.equals("OK")) {
                            System.out.println("Failed - " + response);
                            dataSocketThread = new DataSocketThread(dataSocket, received);
                            dataSocketThread.run();
                        } else {
                            System.out.println(response);
                        }
                    } else {
                        dataOutputStream.writeUTF("Wrong token, try your request again");
                        // NOT SURE ???????
                        //sout ?
                        continue;
                    }

                }
                //System.out.println(socket + " => " + received);
                dataOutputStream.writeUTF(("You: " + received));
                dataOutputStream.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }

            //
        }
        try {
            assert socket != null;
            socket.close();
            if (dataSocket != null) dataSocket.close();
            dataOutputStream.close();
            dataInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private String giveToken (String username) {
        Random rand = new Random();
        return (username+Integer.toString((rand.nextInt())).hashCode());
    }
    private static String stringReaderFromByteArray(InputStream is,int lengthOfMessage){

        String clientString="error";// it will be change. It is initially error because we want to detect whether there is a change
        /**
         * This methos is taking inputStream and Length of the message.
         *
         * StringReaderFromByteArray reads following string which is encoded to byte array.
         *
         * Finally, converts to byte array to a string.
         * */
        try{

            // System.out.println(lengthOfMessage);
            byte[] clientAnswerByteArray=new byte[lengthOfMessage];

            is.read(clientAnswerByteArray,0,lengthOfMessage);

            clientString=byteArrayToString(clientAnswerByteArray,lengthOfMessage);
            // System.out.println(clientString);

        }catch (IOException e){
            e.printStackTrace();
        }
        return clientString;
    }
    private static String byteArrayToString (byte[] byteArrayOfString,int lengthOfMessage){
        /**
         * We are decoding byteArray. We know the lengthOfTheMessage with the help of findLengthOfString Method.
         * */
        char characterOfString='a';
        StringBuilder buildedString = new StringBuilder();


        for(int i=0;i<lengthOfMessage;i++){

            characterOfString=(char)byteArrayOfString[i];
            buildedString.append(characterOfString);

        }
        String finalString=buildedString.toString();


        return finalString;
    }
    private static byte[] intTo4Bytes( final int i ) {
        /**
         * This method allow us to convert our integer value to 4 byte and saves it to a byte array.
         * */
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(i);
        return bb.array();
    }
    private static int findLengthOfString(byte[] intBytes){

        /**
         * This method is finding a String length from an application header.
         *
         * We are using this information to read following string from inputStream
         * */
        byte[] lengthFinder=new byte[4];
        for(int i=2;i<6;i++){
            lengthFinder[i-2]=intBytes[i];
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(lengthFinder);
        return byteBuffer.getInt();
    }
    public static byte[] StringToByteArray(int phase ,int type,String message, byte[] messageByteArray){
        /**
         * This method converts String to byte array with application header
         * */

        char[] myCharArray= message.toCharArray();
        byte zeroByte=(byte) phase;//phase byte
        byte oneByte=(byte) type;//type byte
        int length=message.length();//length of the message to add before the mesage(4byte)
        messageByteArray[0]=(zeroByte);
        messageByteArray[1]=(oneByte);
        byte[] applicationHeader=intTo4Bytes(length);//creating an array consist of length of string
        /**
         * intTo4Bytes creates an byteArray consist of length of the message.
         *
         * Code writes same byte array to correct part of the message
         * */
        for(int i=2;i<(6);i++){
            messageByteArray[i]=applicationHeader[i-2];//adding that array inside of application header
        }
        for (int i=6;i<(message.length()+6);i++){
            Byte k=(byte) myCharArray[i-6];
            messageByteArray[i]=k;//adding the message top of application header(all)
        }
        //Our message as a byte array is ready.
        return messageByteArray;
    }
}
