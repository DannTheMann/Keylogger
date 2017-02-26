package dja33.msc.ukc.msc_log;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dante on 16/02/2017.
 */
public class FileHandler {

    private static final String APP_DIRECTORY = "Keylogger";
    private final Context CONTEXT;
    private final String DIRECTORY;
    private final File fir;
    private boolean read;
    private final String FILE_NAME;
    private final String FILE;
    private final List<String> contents = new ArrayList<>();

    public FileHandler(String directory, String fileName, Context context) {
        this.DIRECTORY = directory + File.separator + APP_DIRECTORY;
        this.fir = new File(directory);
        this.FILE_NAME = fileName;
        this.FILE = DIRECTORY + File.separator + FILE_NAME;
        this.CONTEXT = context;
        System.out.println("Directory: " + FILE);
        new File(DIRECTORY).mkdirs();
        try {
            new File(FILE).createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void delete(){
        new File(FILE_NAME).delete();
        contents.clear();
    }

    public boolean readFile() throws Exception {

        if(read)
            return false;

        try {

            InputStream inputStream = CONTEXT.openFileInput(FILE_NAME);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String receiveString;
                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    contents.add(receiveString);
                }

                inputStream.close();
                bufferedReader.close();

            }else{
                return false;
            }

            read = true;

        }catch(Exception e){
            return false;
        }

        return true;

    }

    private OutputStreamWriter outputStreamWriter;

    public boolean openOutput(){
        try {
            outputStreamWriter = new OutputStreamWriter(CONTEXT.openFileOutput(FILE_NAME, Context.MODE_PRIVATE));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean closeOut(){
        try {
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void writeOut(String msg) throws Exception{
        outputStreamWriter.write(msg + System.lineSeparator());
        contents.add(msg + System.lineSeparator());
    }

    public ArrayList<String> getFileElements(){
        return new ArrayList<>(contents);
    }

    public boolean read(){
        return read;
    }

    public String getFilePath(){
        return FILE;
    }

}
