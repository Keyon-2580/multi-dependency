package cn.edu.fudan.se.multidependency;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserters;

public class BatchInsertDocTest {
	static boolean delFile(File file) {
        if (!file.exists()) {
            return false;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                delFile(f);
            }
        }
        return file.delete();
    }
//	@Test
	public void insert() throws Exception {
		BatchInserter inserter = null;
		try
		{
			File file = new File("D:\\fan\\Users\\Thinkpad\\neo4j-community-3.5.3-windows\\neo4j-community-3.5.3\\data\\databases\\multiple.dependency.db");
			delFile(file);
		    inserter = BatchInserters.inserter( file );
//		            new File( "target/batchinserter-example" ) );

		    Label personLabel = Label.label("Person");
		    inserter.createDeferredSchemaIndex( personLabel ).on( "name" ).create();
		    
		    Map<String, Object> properties = new HashMap<>();

		    properties.put( "name", "Mattias" );
		    long mattiasNode = inserter.createNode( properties, personLabel );

		    properties.put( "name", "Chris" );
		    long chrisNode = inserter.createNode( properties, personLabel );

		    RelationshipType knows = RelationshipType.withName( "KNOWS" );
		    inserter.createRelationship( mattiasNode, chrisNode, knows, null );
		}
		finally
		{
		    if ( inserter != null )
		    {
		        inserter.shutdown();
		    }
		}	
	}

}