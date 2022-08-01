package io.github.sinri.keel.core.helper;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.properties.KeelOptions;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @since 2.6
 */
public class KeelFileHelper {
    private static final KeelFileHelper instance = new KeelFileHelper();

    private KeelFileHelper() {

    }

    public static KeelFileHelper getInstance() {
        return instance;
    }

    public byte[] readFileAsByteArray(String filePath, boolean seekInsideJarWhenNotFound) throws IOException {
        try {
            return Files.readAllBytes(Path.of(filePath));
        } catch (IOException e) {
            if (seekInsideJarWhenNotFound) {
                URL resource = KeelOptions.class.getClassLoader().getResource(filePath);
                if (resource == null) {
                    throw new IOException("Embedded one is not found after not found in FS: " + filePath, e);
                }
                String file = resource.getFile();
                return Files.readAllBytes(Path.of(file));
            } else {
                throw e;
            }
        }
    }

    /**
     * @param filePath path string of the target file, or directory
     * @return the URL of target file; if not there, null return.
     */
    public URL getUrlOfFileInJar(String filePath) {
        return KeelOptions.class.getClassLoader().getResource(filePath);
    }

    /**
     * Seek in JAR, under the root (exclusive)
     *
     * @param root ends with '/'
     * @return list of JarEntry
     */
    public List<JarEntry> traversalInJar(String root) {
        List<JarEntry> jarEntryList = new ArrayList<>();
        try {
            // should root ends with '/'?
            URL url = Keel.class.getClassLoader().getResource(root);
            if (url == null) {
                throw new RuntimeException("Resource is not found");
            }
            if (!url.toString().contains("!/")) {
                throw new RuntimeException("Resource is not in JAR");
            }
            String jarPath = url.toString().substring(0, url.toString().indexOf("!/") + 2);

            URL jarURL = new URL(jarPath);
            JarURLConnection jarCon = (JarURLConnection) jarURL.openConnection();
            JarFile jarFile = jarCon.getJarFile();
            Enumeration<JarEntry> jarEntries = jarFile.entries();
            var baseJarEntry = jarFile.getJarEntry(root);
            var pathOfBaseJarEntry = Path.of(baseJarEntry.getName());

            while (jarEntries.hasMoreElements()) {
                JarEntry entry = jarEntries.nextElement();

                Path entryPath = Path.of(entry.getName());
                if (entryPath.getParent() == null) {
                    continue;
                }
                if (entryPath.getParent().compareTo(pathOfBaseJarEntry) == 0) {
                    jarEntryList.add(entry);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return jarEntryList;
    }
}
