package com.weatnet;

import com.solidfire.gson.Gson;
import com.solidfire.gson.GsonBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Scanner;

public class Api {

    ArrayList<City> cities = new ArrayList<>();

    BufferedImage bufferedImage;
    City targetCity;
    private String message;
    private String type;
    String cityName;
    final String apiKey = "6b4255bf7369c755b4bc5e98cfe5bb4e";
    final String base = "http://api.openweathermap.org/data/2.5/";
    String core;
    URL url;

    public Api(String cityName, String type) {
        cities.add(new City("Istanbul", 745044, 28.949659, 41.01384, 8, 148, 95));
        cities.add(new City("Samsun", 740264, 36.330002, 41.286671,10, 615, 382));
        cities.add(new City("Eskişehir", 315201, 31.16667, 39.666672, 11, 1197, 777));
        cities.add(new City("Ankara", 323784, 32.833328, 39.916672,9, 302, 194));
        cities.add(new City("Malatya", 304919, 38.0, 38.5, 10, 621, 393));
        cities.add(new City("Bursa", 750268, 29.08333, 40.166672, 10, 595, 387));
        cities.add(new City("Izmir", 311044, 27.092291, 38.462189, 10, 589, 393));
        this.cityName = cityName;
        this.type = type;
    }

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public void setBufferedImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    public void fetchData() {
        targetCity = new City();
        for (City city : cities) {
            if (city.getName().equalsIgnoreCase(cityName)) {
                targetCity = city;
            }
        }//current, minutely, hourly, daily,
        if (type.contains("current") || type.contains("minutely") || type.contains("hourly") || type.contains("daily")) {
            core = "onecall?lat=" + targetCity.getLat() + "&lon=" + targetCity.getLon() + "&lang=tr&units=metric&appid=";
            try {
                //System.out.println(requestTypes.get(type));
                url = new URL(base + core + apiKey);
                System.out.println(url);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                int responseCode = connection.getResponseCode();

                if (responseCode != 200) {
                    throw new RuntimeException("HttpResponseCode: " + responseCode);
                }
                else {
                    readUrl(url);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (type.contains("clouds") || type.contains("pressure") || type.contains("wind") || type.contains("temp")){
            try {
                url = new URL("https://tile.openweathermap.org/map/" + type + "_new/" + targetCity.getZ() + "/" + targetCity.getX() + "/" + targetCity.getY() + ".png?appid=" + apiKey);
                handleImage();
                setMessage("");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else if (type.contains("historical")) {
            int aDay = 86400;
            core = "onecall/timemachine?lat=" + targetCity.getLat() + "&lon=" + targetCity.getLon() + "&dt=" + (Instant.now().getEpochSecond()) + "&lang=tr&units=metric&appid=";
            try {
                url = new URL(base + core + apiKey);
                System.out.println(url);
                HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    throw new RuntimeException("HttpResponseCode: " + responseCode);
                }
                else {
                    readUrl(url);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            setMessage("unknown command");
        }


    }

    private void handleImage() {
        System.out.println(url);
        try {
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int responseCode = connection.getResponseCode();

            if (responseCode != 200) {
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            }
            else {
                downloadImage(url);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadImage(URL url) {
        try {
            setBufferedImage(ImageIO.read(new URL(url.toString())));

            File outputFile = new File("downloads",targetCity.getName() + "_" + type + "_" + Instant.now() + ".png");
            ImageIO.write(getBufferedImage(), "png", outputFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void readUrl(URL url) {
        try {
            Scanner scanner = new Scanner(url.openStream());
            StringBuilder inline = new StringBuilder();
            while (scanner.hasNext()) {
                inline.append(scanner.nextLine());
            }
            scanner.close();
            handleJSON(inline);
            //printData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleJSON(StringBuilder inline) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(String.valueOf(inline));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String prettyJsonString = gson.toJson(jsonObject.get(type));
            setMessage(prettyJsonString);
            FileWriter file = new FileWriter(new File("downloads",targetCity.getName() + "_" + type + "_" + Instant.now() + ".json"));

            file.write(getMessage());
            file.flush();


        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String msg) {
        message = msg;
    }

    /*
    public static void main(String[] args) {
        // DONE: current, minutely forecast, hourly forecast, daily forecast,

        TYPES : current, minutely, hourly, daily, lat, lon, timezone
        CITIES: Istanbul, Samsun, Eskişehir, Ankara, Malatya, Bursa, İzmir

        Api api = new Api("Istanbul", "current");
        api.fetchData();
        System.out.println(api.getMessage());
    }
    */
}
