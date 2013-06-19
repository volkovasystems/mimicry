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

@SuppressWarnings("UseSpecificCatch")
public abstract class Mimicry{
	
	public static interface Parameters{
		public String getValue( String option );
		public int getOrderOf( String option );
		public String getOrderAt( int index );
		public Map< String, String > getParameters( );
		public Map< String, String > getParametersOrder( );
	}

	private static class Configuration implements Parameters{
		private Map< String, String > parameters = null;
		private Map< String, String > order = null;
		public Configuration( String... arguments ){
			parameters = new HashMap< >( arguments.length );
			order = new HashMap< >( );
			String key = "";
			String value = "";
			String[] argument = null;
			for( int index = 0 ; index < arguments.length ; index++ ){
				argument = arguments[ index ].split( "=" );
				if( argument.length == 2 ){
					key = argument[ 0 ];
					value = argument[ 1 ];
				}else{
					key = "" + index;
					value = arguments[ 0 ];
				}
				if( key != ( "" + index ) ){
					order.put( "" + index, key );	
				}
				order.put( key, "" + index );
				parameters.put( key, value );
			}
		}
		
		@Override
		public String getValue( String option ){
			return parameters.get( option );
		}

		@Override
		public int getOrderOf( String option ){
			try{
				return Integer.parseInt( order.get( option ) );
			}catch( Exception exception ){
				return -1;
			}
		}

		@Override
		public String getOrderAt( int index ){
			return order.get( "" + index );
		}

		@Override
		public Map< String, String > getParameters( ){
			return parameters;
		}

		@Override
		public Map< String, String > getParametersOrder( ){
			return order;
		}
	}

	private static boolean isLoaded = false;
	private static Map< String, Mimicry > mimics = null;
	private static Class< ? extends Mimicry > mimicClass = null;
	private static Class< ? > coreClass = null;
	private static BufferedReader reader = null;
	
	private static final String MIMICRY = "Mimicry";

	public static final String LOAD = "load";
	public static final String EXECUTE = "execute";
	public static final String EXIT = "exit";
	public static final String HASH = "hash";
	public static final String COMMANDS = "commands";
	public static final String STATUS = "status";
	public static final String MODE = "mode";
	public static final String EXTENDED = "extended";
	public static final String CORE = "core";
	public static final String CLASS = "class";


	private Configuration config = null;
	private String arguments = null;

	public Mimicry( String... arguments ){
		this.arguments = arguments.toString( );
		this.config = new Configuration( arguments );
	}
	public Mimicry( ){ }

	/*
		We have a problem here. We have to support command list for
			extended implementation of Mimicry engine.
		If the engine provides a core class it should add to
			the list of commands.

		We also needs some sort of validation if the command
			is acceptable for release.

		We also needs to restrict Mimicry class abstracted methods.

	*/
	public String[] getCommandList( ){
		List< String > methodNames = new ArrayList< >( );
		Method[] methods = null;
		if( coreClass != null ){
			methods = coreClass.getDeclaredMethods( );
			for( Method method: methods ){
				methodNames.add( method.getName( ) );
			}
		}
		if( !this.getClass( ).getName( ).contains( MIMICRY ) ){
			methods = this.getClass( ).getDeclaredMethods( );
			for( Method method : methods ){
				methodNames.add( method.getName( ) );		
			}
			//Restriction to Mimicry class's methods is bound to this abstracted method.
			return this.restriction( methodNames
				.toArray( new String[ methodNames.size( ) ] ) );
		}
		return null;
	}

	public Parameters getConfiguration( ){
		return this.config;
	}

	public void setConfiguration( String... arguments ){
		this.arguments = arguments.toString( );
		this.config = new Configuration( arguments );
	}

	public String getHash( ){
		Iterator< String > keys = mimics.keySet( ).iterator( );
		String key = null;
		while( keys.hasNext( ) ){
			if( mimics.get( ( key = keys.next( ) ) )
				.equals( this ) )
			{
				return key;
			}
		}
		return ( "" + this.hashCode( ) );
	}

	@Override
	public String toString( ){
		//This gives the current state of the mimic engine.
		return ( this.getHash( ) + ":" + this.arguments );
	}

	/*
		I'm planning to use the process method and should be overriden
			by the sub class. But it also lets the coder of the sub class
			to call the super process which is a burden ( what if he forgots? disastrous )

		This process now will handle the core procedures if the program
			wanted to extract meta information.

		This process is standard to all sub classes of Mimicry.
	
		The commands here should have no parameters because this is
			simply getting data.
	
		This process provides the following meta information processing:
		1. COMMANDS - list the commands of the sub class or the core class.
		2. MODE - loaded or no mode
		3. EXTENDED - return true or false
		4. CORE - returns the name of the core class
		5. CLASS - returns the name of the mimicry class engine
	*/
	public String thisProcess( ){
		String command = this.arguments.split( " " )[ 0 ];
		switch( command ){
			case COMMANDS:
				return Arrays.toString( this.getCommandList( ) ).replaceAll( "\\[|\\]|,", "" );
			
			case MODE:
				return ( ( isLoaded )? "loaded" : "" );
			
			case EXTENDED:
				return "" + ( coreClass != null );
			
			case CORE:
				return coreClass.getName( );

			case CLASS:
				return mimicClass.getName( );

			default:
				return null;
		}
	}



	/*
		This will be used by the extending class to implements
			how it will process the parameters to the matching methods.
	*/
	public abstract String process( );

	/*
		This will restrict the mimicry engine to
			provide only usable commands.
	*/
	public abstract String[] restriction( String[] commands );


	/*
		If the core class is not initialize using the static constructor
			of the mimic class expect bad results.	
	*/
	public static void main( String... arguments ){
		if( arguments.length == 0 ){
			outputResult( "warning:no meta command" );
			System.exit( 0 );		
		}
		identifyClass( );
		try{
			Mimicry mimic = null;
			String[] previousArguments = null;
			String hash = null;
			RELOAD:
			switch( arguments[ 0 ] ){
				case LOAD:
					if( !isLoaded ){
						isLoaded = true;
					}
					if( mimic == null ){
						mimic = createMimicry( );	
					}
					if( arguments.length > 1 ){
						hash = arguments[ 1 ];
						if( hash.toLowerCase( ).equals( "true" ) ){
							hash = "" + mimic.hashCode( );
						}else{
							mimic = mimics.get( hash );	
						}
					}
					if( arguments.length > 2 
						&& arguments[ 2 ].toLowerCase( ).equals( "true" ) )
					{
						if( mimics == null ){
							mimics = new HashMap< >( );
						}
						mimics.put( hash, mimic );
					}
					
					previousArguments = arguments;
					arguments = listenInput( );

				    
				case EXECUTE:
					String[] argument = Arrays.asList( arguments )
						.subList( 1, arguments.length - 1 )
						.toArray( new String[ arguments.length - 1 ] );
					if( mimic == null ){
						mimic = createMimicry( argument );
					}else if( argument.toString( )
						.equals( mimic.toString( ).split( ":" )[ 1 ] ) )
					{
						mimic.setConfiguration( argument );
					}
					String result = mimic.thisProcess( );
					if( result == null ){
						result = mimic.process( );
					}
					outputResult( "result@" + hash + ":" + result );
					
					if( isLoaded ){
						arguments = previousArguments;
						mimic = null;
						break RELOAD;
					}
					break;


				case EXIT:
					System.exit( 0 );
			}
		}catch( Exception exception ){
			outputResult( "error:" + exception.getMessage( ) );
		}
	}

	public static void identifyClass( ){
		/*
			This will read the directory where this class belongs.
			Generally, the mimicry class engine (sub class) will
				register a core class or use itself.
			But the engine will not recognize itself
				just by using the static context.
			So to solve this issue, we will try to do the following:
			1. Read the self directory and load all possible classes
				without the $ sign (for internal and anonymous classes)
			2. Try to check using the class loader context if the
				class is loaded in this class loader.
			3. Try to check if this is a Mimicry class if not
				check if this is a sub class of Mimicry class
			4. If this is a sub class of Mimicry class and 
				it is the currently loaded class then voila
				we are using the correct mimicry engine class.
		*/
		File directory = new File( "./" );
		if( directory.isDirectory( ) ){
			File[] classes = directory.listFiles( new FilenameFilter( ){
				@Override
				public boolean accept( File directory, String name ){
					return name.contains( ".class" ) && !name.contains( "$" );
				}
			} );
			ClassLoader systemLoader = ClassLoader.getSystemClassLoader( );
			try{
				Method findLoadedClass = ClassLoader.class.getDeclaredMethod( "findLoadedClass", 
					new Class[ ]{ String.class } );
				findLoadedClass.setAccessible( true );
				Class< ? extends Mimicry > mimicryClass = null;
				String name = null;
				for( File file : classes ){
					name = file.getName( ).split( "." )[ 0 ];
					if( Boolean.class.cast( findLoadedClass.invoke( systemLoader, name ) ).booleanValue( ) ){
						//We have a problem on this we have to check for ClassCastException
						try{
							mimicryClass = ( Class< ? extends Mimicry > ) Class.forName( name );
							if( mimicryClass.getClass( ).getSuperclass( ).getName( ).contains( MIMICRY ) ){
								mimicClass = mimicryClass;
								return;
							}
						}catch( ClassCastException exception ){ /* Don't do anything just catch it. */ }
					}
				}
			}catch( Exception exception ){
				outputResult( "error:" + exception.getMessage( ) );
			}
		}	
		outputResult( "error:cannot identify engine class" );
		if( !isLoaded ){
			System.exit( 0 );	
		}	
	}

	
	public static Mimicry createMimicry( String... arguments ){
		Constructor constructor = null;
		try{
			if( arguments.length > 0 ){
				constructor = mimicClass.getDeclaredConstructor( String[].class );
			}else{
				constructor = mimicClass.getDeclaredConstructor( );
			}
			Mimicry mimicry = Mimicry.class.cast( constructor
				.newInstance( ( Object[ ] ) arguments ) );
			if( mimicry.config == null ){
				//The coder is dumb he did not use super( this );
				mimicry.setConfiguration( arguments );
			}
			return mimicry;
		}catch( Exception exception ){
			return null;
		}
	}

	/*
		There are only 3 types of result.
		1. Result
		2. Error
		3. Warning
	*/
	public static void outputResult( String result ){
		if( result.contains( "error:" ) ){
			System.err.println( result );
		}else{
			System.out.println( result );	
		}
	}

	public static String[] listenInput( ){
		try{
			if( isLoaded ){
				if( reader == null ){
					reader = new BufferedReader( new InputStreamReader( System.in ) );
				}
				return reader.readLine( ).split( " " );
			}
		}catch( Exception exception ){
			if( isLoaded ){
				reader = new BufferedReader( new InputStreamReader( System.in ) );
			}
		}
		return null;
	}
}