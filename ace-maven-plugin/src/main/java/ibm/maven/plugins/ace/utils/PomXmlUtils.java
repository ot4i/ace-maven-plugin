package ibm.maven.plugins.ace.utils;

import java.io.File;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.JAXBIntrospector;
import jakarta.xml.bind.Unmarshaller;

import ibm.maven.plugins.ace.generated.maven_pom.Model;

/**
 * @author u209936
 * 
 */
public class PomXmlUtils {

    /**
     * @param pomFile
     * @return
     * @throws JAXBException
     */
    public static Model unmarshallPomFile(File pomFile)
            throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(Model.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (Model) JAXBIntrospector.getValue(unmarshaller.unmarshal(pomFile));

    }
}
