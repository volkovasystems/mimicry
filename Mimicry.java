import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class Mimicry{
    
    private Class mimicClass;
    
    public Mimicry( Class mimicClass ){
        this.mimicClass = mimicClass;
    }
  
    public void addMethod( String methodName ){
        
    }
    
    public void listen( ){
        try{
            BufferedReader reader = new BufferedReader( new InputStreamReader( System.in ) );
            String input = reader.readLine( );
        }catch( Exception exception ){
            
        }
    }
}