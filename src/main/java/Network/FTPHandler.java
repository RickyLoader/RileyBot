package Network;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Receiving files from a server via FTP
 */
public class FTPHandler {
    private final String ip, username, password;
    private final int port;
    private final FTPClient client;

    /**
     * Initialise the FTP handler
     *
     * @param ip       IP address of server
     * @param port     FTP port
     * @param username Username
     * @param password Password
     */
    public FTPHandler(String ip, int port, String username, String password) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
        this.client = new FTPClient();
        connect(client);
    }

    /**
     * Get an input stream of a file by its path
     *
     * @param path Path to file
     * @return Input stream of file or null
     */
    public InputStream getFileAsStream(String path) {
        try {
            connect(client);
            return client.retrieveFileStream(path);
        }
        catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get a file as a String by its path
     *
     * @param path Path to file
     * @return String of file or null
     */
    public String getFileAsString(String path) {
        try {
            InputStream fileStream = getFileAsStream(path);
            String result = IOUtils.toString(fileStream, StandardCharsets.UTF_8);
            fileStream.close();
            return result;
        }
        catch(IOException e) {
            return null;
        }
    }

    /**
     * Get an array of files in the given directory
     *
     * @param directory Directory path
     * @return Array of files in the directory
     */
    public FTPFile[] getDirectoryContents(String directory) {
        try {
            return client.listFiles(directory);
        }
        catch(IOException e) {
            return null;
        }
    }

    /**
     * Connect and login to the FTP server
     *
     * @param client Client to connect with
     */
    public void connect(FTPClient client) {
        try {
            client.connect(ip, port);
            client.enterLocalPassiveMode();
            client.login(username, password);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
}
