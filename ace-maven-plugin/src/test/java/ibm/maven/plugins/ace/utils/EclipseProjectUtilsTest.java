package ibm.maven.plugins.ace.utils;

import static org.junit.Assert.assertTrue;
import ibm.maven.plugins.ace.utils.EclipseProjectUtils;

import java.io.File;

import javax.xml.bind.JAXBException;

import org.junit.Test;

public class EclipseProjectUtilsTest {

    /**
     * Test the unmarshalling of a .project file
     */
    @Test
    public void unmarshallEclipseProjectFileTest() {
        try {
            // somewhat lazy, but test with the local .project file
            EclipseProjectUtils.unmarshallEclipseProjectFile(new File(".project"));
        } catch (JAXBException e) {
            e.printStackTrace();
            assertTrue("An error occurred. See stack trace.", false);
        }
    }

    /**
     * Hier sollten vor allem:
     * - isApplication
     * - isLibrary
     * getestet werden.
     * 
     * Ein solcher Test ist aber sehr aufwendig.
     */

}
