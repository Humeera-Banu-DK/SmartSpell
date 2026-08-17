package utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class DictionaryImporter {

    private static final String DICTIONARY_URL =
            "https://raw.githubusercontent.com/dwyl/english-words/master/words_alpha.txt";

    public static void main(String[] args) {

        int count = 0;

        String sql =
                "INSERT IGNORE INTO dictionary_words (word) VALUES (?)";

        try {

            System.out.println("Connecting to MySQL...");

            Connection connection =
                    DBConnection.getConnection();

            if (connection == null) {

                System.out.println(
                        "Database connection failed."
                );

                return;
            }

            System.out.println(
                    "Connected to MySQL successfully."
            );

            System.out.println(
                    "Downloading dictionary..."
            );


            URL url =
                    new URL(DICTIONARY_URL);

            HttpURLConnection connectionHttp =
                    (HttpURLConnection) url.openConnection();

            connectionHttp.setRequestMethod("GET");

            connectionHttp.setConnectTimeout(10000);

            connectionHttp.setReadTimeout(30000);


            if (connectionHttp.getResponseCode() != 200) {

                System.out.println(
                        "Unable to download dictionary."
                );

                System.out.println(
                        "HTTP Status: "
                        + connectionHttp.getResponseCode()
                );

                return;
            }


            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    connectionHttp.getInputStream()
                            )
                    );


            PreparedStatement statement =
                    connection.prepareStatement(sql);


            String word;

            while ((word = reader.readLine()) != null) {

                word = word.trim().toLowerCase();

                if (word.isEmpty()) {
                    continue;
                }

                statement.setString(1, word);

                statement.addBatch();

                count++;

                // Insert every 1000 words
                if (count % 1000 == 0) {

                    statement.executeBatch();

                    System.out.println(
                            count
                            + " words processed..."
                    );
                }
            }


            // Insert remaining words
            statement.executeBatch();


            reader.close();

            statement.close();

            connection.close();

            connectionHttp.disconnect();


            System.out.println();
            System.out.println(
                    "Dictionary import completed!"
            );

            System.out.println(
                    "Words processed: "
                    + count
            );


        } catch (Exception e) {

            System.out.println(
                    "Something went wrong!"
            );

            e.printStackTrace();
        }
    }
}