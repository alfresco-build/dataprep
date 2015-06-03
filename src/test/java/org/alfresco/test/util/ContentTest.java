package org.alfresco.test.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.test.util.CMISUtil.DocumentType;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.alfresco.api.entities.Site.Visibility;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test CMIS crud operations.
 * 
 * @author Bogdan Bocancea
 * @author Cristina Axinte
 */

public class ContentTest extends AbstractTest
{    
    @Autowired private UserService userService;
    @Autowired private SiteService site;
    @Autowired private ContentService content;
    String admin = "admin";
    String password = "password";
    String folder = "cmisFolder";
    String plainDoc = "plainDoc";
    
    
    @Test(expectedExceptions = CmisRuntimeException.class)
    public void testCreateFolderTwice() throws Exception
    {
        String siteName = "siteCMIS-" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();    
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        content.createFolder(userName, password, folder, siteName);
        content.createFolder(userName, password, folder, siteName);     
    }
    
    @Test 
    public void testCreateFolder() throws Exception
    {
        String siteName = "siteCMIS-" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        Folder newFolder = content.createFolder(userName, password, folder, siteName);
        Assert.assertFalse(newFolder.getId().isEmpty());
        Folder parent = newFolder.getFolderParent();
        Assert.assertEquals(parent.getName(), "documentLibrary");      
    }
    
    @Test(expectedExceptions = CmisRuntimeException.class)
    public void testCreateFolderInvalidSimbols() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        String symbolFolder = "*/.:?|\\`\"";
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        content.createFolder(userName, password, symbolFolder, siteName);
    }
    
    @Test(expectedExceptions = RuntimeException.class)
    public void createFolderInvalidUser() throws Exception
    {
        String siteName = "siteCMIS-" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();   
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        content.createFolder("fakeUser", "fakePass", folder, siteName);
    }
    
    @Test(expectedExceptions = CmisRuntimeException.class)
    public void createFolderInvalidSite() throws Exception
    {
        String userName = "cmisUser" + System.currentTimeMillis();    
        userService.create(admin, admin, userName, password, password);
        content.createFolder(userName, password, folder, "fakeSite");
    }
    
    @Test
    public void testDeleteFolders() throws Exception
    {
        String siteName = "siteCMISDelete" + System.currentTimeMillis();
        String userName = "cmisUserDelete" + System.currentTimeMillis();
        String rootFolder = "cmisFolderDelete";
        String secondFolder = "cmisSecondFolder";   
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        Folder rootFld = content.createFolder(userName, password, rootFolder, siteName);
        Assert.assertFalse(rootFld.getId().isEmpty());    
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        properties.put(PropertyIds.NAME, secondFolder);
        Folder secondFld = rootFld.createFolder(properties);
        Assert.assertFalse(secondFld.getId().isEmpty());       
        content.deleteFolder(userName, password, siteName, secondFolder);
        content.deleteFolder(userName, password, siteName, rootFolder);
        Assert.assertTrue(content.getNodeRef(userName, password, siteName, rootFolder).isEmpty());
        Assert.assertTrue(content.getNodeRef(userName, password, siteName, secondFolder).isEmpty());
    }
    
    @Test(expectedExceptions = CmisRuntimeException.class)
    public void deleteNonExistentFolder() throws Exception
    {
        String userName = "cmisUserDelete" + System.currentTimeMillis(); 
        String siteName = "siteCMISDelete" + System.currentTimeMillis();
        userService.create(admin, admin, userName, password, password);            
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                "my site description", 
                Visibility.PUBLIC);
        content.deleteFolder(userName, password, siteName, "fakeFolder");      
    }
    
    @Test
    public void testCreateDocument() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        String plainDoc = "plain";
        String msWord = "msWord";
        String msExcel = "msExcel";
        String html = "html";
        String xml = "xml";
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        Document doc1 = content.createDocument(userName, password, siteName, DocumentType.TEXT_PLAIN, plainDoc, plainDoc);
        Assert.assertFalse(doc1.getId().isEmpty());
        Document doc2 = content.createDocument(userName, password, siteName, DocumentType.MSWORD, msWord, msWord);
        Assert.assertFalse(doc2.getId().isEmpty());
        Document doc3 = content.createDocument(userName, password, siteName, DocumentType.MSEXCEL, msExcel, msExcel);
        Assert.assertFalse(doc3.getId().isEmpty());
        Document doc4 = content.createDocument(userName, password, siteName, DocumentType.HTML, html, html);
        Assert.assertFalse(doc4.getId().isEmpty());
        Document doc5 = content.createDocument(userName, password, siteName, DocumentType.XML, xml, xml);
        Assert.assertFalse(doc5.getId().isEmpty());
    }
    
    @Test(expectedExceptions = CmisRuntimeException.class)
    public void testCreateDocInvalidSimbols() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        String symbolDoc = "*/.:?|\\`\"";
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        content.createDocument(userName, password, siteName, DocumentType.TEXT_PLAIN, symbolDoc, symbolDoc);
    }
    
    @Test(expectedExceptions = CmisRuntimeException.class)
    public void createDocFakeSite() throws Exception
    {
        String userName = "cmisUser" + System.currentTimeMillis();  
        userService.create(admin, admin, userName, password, password);
        content.createDocument(userName, password, "fakeSite", DocumentType.TEXT_PLAIN, plainDoc, plainDoc);
    }
    
    @Test(expectedExceptions = CmisRuntimeException.class)
    public void createDuplicatedDoc() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        content.createDocument(userName, password, siteName, DocumentType.TEXT_PLAIN, plainDoc, plainDoc);
        content.createDocument(userName, password, siteName, DocumentType.TEXT_PLAIN, plainDoc, plainDoc);
    }
    
    @Test
    public void createDocumentInFolder() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        content.createFolder(userName, password, folder, siteName);
        Document doc = content.createDocumentInFolder(userName, password, siteName, folder, DocumentType.TEXT_PLAIN, plainDoc, plainDoc);
        Assert.assertFalse(doc.getId().isEmpty());
    }
    
    @Test(expectedExceptions = CmisRuntimeException.class)
    public void createDocInFolderInvalidSymbols() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        String symbolDoc = "*/.:?|\\`\"";
        String folder = "cmisFolder";
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        content.createFolder(userName, password, folder, siteName);
        content.createDocumentInFolder(userName, password, siteName, folder, DocumentType.TEXT_PLAIN, symbolDoc, symbolDoc);
    }
    
    @Test(expectedExceptions = CmisRuntimeException.class)
    public void createDocInNonExistentFolder() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);    
        content.createDocumentInFolder(userName, password, siteName, "fakeFolder", DocumentType.TEXT_PLAIN, plainDoc, plainDoc);
    }
    
    @Test
    public void createDocumentInSubFolder() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        String subFolderDoc = "cmisDoc" + System.currentTimeMillis();       
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);     
        Folder folderRoot = content.createFolder(userName, password, folder, siteName);
        content.createDocumentInFolder(userName, password, siteName, folder, DocumentType.TEXT_PLAIN, plainDoc, plainDoc);   
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        properties.put(PropertyIds.NAME, folder);
        folderRoot.createFolder(properties);
        Document subDoc = content.createDocumentInFolder(userName, password, siteName, folder, DocumentType.MSWORD, subFolderDoc, subFolderDoc);
        Assert.assertFalse(subDoc.getId().isEmpty());
    }
    
    @Test
    public void deleteDocumentFromRoot() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        content.createDocument(userName, password, siteName, DocumentType.TEXT_PLAIN, plainDoc, plainDoc);
        content.deleteDocument(userName, password, siteName, plainDoc);
        Assert.assertTrue(content.getNodeRef(userName, password, siteName, plainDoc).isEmpty());
    }
    
    @Test
    public void deleteDocumentFromFolder() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        content.createFolder(userName, password, folder, siteName);
        content.createDocumentInFolder(userName, password, siteName, folder, DocumentType.TEXT_PLAIN, plainDoc, plainDoc);
        content.deleteDocument(userName, password, siteName, plainDoc);
        Assert.assertTrue(content.getNodeRef(userName, password, siteName, plainDoc).isEmpty());
    }
    
    @Test(expectedExceptions = CmisRuntimeException.class)
    public void deleteDocumentInvalidSite() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        content.createFolder(userName, password, folder, siteName);
        content.createDocument(userName, password, siteName, DocumentType.TEXT_PLAIN, plainDoc, plainDoc);
        content.deleteDocument(userName, password, "fakeSite", plainDoc);     
    }
    
    @Test
    public void deleteTree() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        String folder1 = "cmisFolder1" +  System.currentTimeMillis();
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        content.createDocument(userName, password, siteName, DocumentType.XML, "xmlDoc", "contentfwfwfwfwgwegwgw");
        Folder f = content.createFolder(userName, password, folder, siteName);
        content.createDocumentInFolder(userName, password, siteName, folder, DocumentType.TEXT_PLAIN, plainDoc, plainDoc);      
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        properties.put(PropertyIds.NAME, folder1);
        f.createFolder(properties);
        content.deleteTree(userName, password, siteName, folder);
        Assert.assertTrue(content.getNodeRef(userName, password, siteName, folder1).isEmpty());
        Assert.assertTrue(content.getNodeRef(userName, password, siteName, plainDoc).isEmpty());       
    }
    
    @Test
    public void testUploadDocsInDocumentLibrary() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        List<Document> uploadedDocs = content.uploadFiles(DATA_FOLDER, userName,password, siteName);
        Assert.assertNotNull(uploadedDocs);
        for (Document d:uploadedDocs)
        {
            Assert.assertFalse(d.getId().isEmpty());
        }       
    }
    
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void testUploadDocsFromFileInsteadFolder() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        String fileName = "cmisFile" + System.currentTimeMillis();
        String fileFromPath = DATA_FOLDER + SLASH + "UploadFile-xml.xml";
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        content.createDocument(userName, password, siteName, DocumentType.TEXT_PLAIN, fileName, "file node");
        content.uploadFiles(fileFromPath, userName, password, siteName);     
    }
    
    @Test
    public void testUploadDocsInFolder() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        String folderName = "cmisFolder" + System.currentTimeMillis();
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        content.createFolder(userName, password, folderName, siteName);
        List<Document> uploadedDocs = content.uploadFilesInFolder(DATA_FOLDER, userName,password, siteName,folderName);
        Assert.assertNotNull(uploadedDocs);
        for (Document d:uploadedDocs)
        {
            Assert.assertFalse(d.getId().isEmpty());
        }       
    }
    
    @Test(expectedExceptions = CmisRuntimeException.class)
    public void testUploadDocsInNonExistentFolder() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        content.uploadFilesInFolder(DATA_FOLDER, userName,password, siteName,"NotExistFld");     
    }
    
    @Test(expectedExceptions = CmisRuntimeException.class)
    public void testUploadDocsInFileInsteadFolder() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        String fileName = "cmisFile" + System.currentTimeMillis();
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        content.createDocument(userName, password, siteName, DocumentType.TEXT_PLAIN, fileName, "file node");
        content.uploadFilesInFolder(DATA_FOLDER, userName,password, siteName, fileName);     
    }
    
    @Test
    public void testCreateDocumentFile() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        String plainDoc = "plain" +  System.currentTimeMillis();
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        File file = new File(plainDoc);
        Document doc1 = content.createDocument(userName, password, siteName, DocumentType.MSWORD, file, plainDoc);
        Assert.assertFalse(doc1.getId().isEmpty());
    }
    
    @Test
    public void testDeleteFiles() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        String plainFile = "UploadFile-plaintext.txt" ;
        String xmlFile = "UploadFile-xml.xml";
        String htmlFile = "UploadFile-html.html";
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        content.uploadFiles(DATA_FOLDER, userName, password, siteName);
        content.deleteFiles(userName, password, siteName, plainFile, xmlFile, htmlFile);  
        Assert.assertTrue(content.getNodeRef(userName, password, siteName, plainFile).isEmpty());
        Assert.assertTrue(content.getNodeRef(userName, password, siteName, xmlFile).isEmpty());
        Assert.assertTrue(content.getNodeRef(userName, password, siteName, htmlFile).isEmpty());
    }
    
    @Test
    public void testDeleteNoFiles() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        String plainFile = "UploadFile-plaintext.txt" ;
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        content.uploadFiles(DATA_FOLDER, userName, password, siteName);
        content.deleteFiles(userName, password, siteName);  
        Assert.assertFalse(content.getNodeRef(userName, password, siteName, plainFile).isEmpty());
    }
    
    @Test
    public void testDeleteFilesFromFolders() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        String folderName= "cmisFolder" + System.currentTimeMillis();
        String plainFile = "UploadFile-plaintext.txt" ;
        String xmlFile = "UploadFile-xml.xml";
        String htmlFile = "UploadFile-html.html";
        String xlxsFile = "UploadFile-xlsx.xlsx" ;
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        content.createDocument(userName, password, siteName, DocumentType.HTML, htmlFile, "content html");
        content.createDocument(userName, password, siteName, DocumentType.TEXT_PLAIN, plainFile, "content plain text");
        content.createFolder(userName, password, folderName, siteName);
        content.createDocumentInFolder(userName, password, siteName, folderName, DocumentType.XML, xmlFile, "content xml");
        content.createDocumentInFolder(userName, password, siteName, folderName, DocumentType.MSEXCEL, xlxsFile, "content excel");
        content.deleteFiles(userName, password, siteName, plainFile, xmlFile, xlxsFile);  
        Assert.assertTrue(content.getNodeRef(userName, password, siteName, plainFile).isEmpty());
        Assert.assertTrue(content.getNodeRef(userName, password, siteName, xmlFile).isEmpty());
        Assert.assertTrue(content.getNodeRef(userName, password, siteName, xlxsFile).isEmpty());
        Assert.assertFalse(content.getNodeRef(userName, password, siteName, htmlFile).isEmpty());
    }
    
    @Test
    public void updateContent() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        String plainDoc = "plain";
        String html = "html";
        String xml = "xml";
        String newContentPlain = "new plain content";
        String newContentHtml = "new html content";
        String newContentXml = "new xml content";      
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        content.createDocument(userName, password, siteName, DocumentType.TEXT_PLAIN, plainDoc, plainDoc);
        content.createDocument(userName, password, siteName, DocumentType.HTML, html, html);
        content.createDocument(userName, password, siteName, DocumentType.XML, xml, xml);
        Assert.assertTrue(content.getDocumentContent(userName, password, siteName, plainDoc).equals(plainDoc));
        Assert.assertTrue(content.getDocumentContent(userName, password, siteName, html).equals(html));
        Assert.assertTrue(content.getDocumentContent(userName, password, siteName, xml).equals(xml));     
        Assert.assertTrue(content.updateDocumentContent(userName, password, siteName, DocumentType.TEXT_PLAIN, plainDoc, newContentPlain));
        Assert.assertTrue(content.updateDocumentContent(userName, password, siteName, DocumentType.HTML, html, newContentHtml));
        Assert.assertTrue(content.updateDocumentContent(userName, password, siteName, DocumentType.XML, xml, newContentXml));       
        Assert.assertTrue(content.getDocumentContent(userName, password, siteName, plainDoc).equals(newContentPlain));
        Assert.assertTrue(content.getDocumentContent(userName, password, siteName, html).equals(newContentHtml));
        Assert.assertTrue(content.getDocumentContent(userName, password, siteName, xml).equals(newContentXml));
    }
    
    @Test
    public void updateContentEmpty() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();
        String plainDoc = "plain";     
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);
        content.createDocument(userName, password, siteName, DocumentType.TEXT_PLAIN, plainDoc, plainDoc);   
        Assert.assertTrue(content.updateDocumentContent(userName, password, siteName, DocumentType.TEXT_PLAIN, plainDoc, ""));    
        Assert.assertTrue(content.getDocumentContent(userName, password, siteName, plainDoc).equals(""));
    }
    
    @Test(expectedExceptions = RuntimeException.class)
    public void updateContentInvalidDoc() throws Exception
    {
        String siteName = "siteDocNew" + System.currentTimeMillis();
        String userName = "cmisUser" + System.currentTimeMillis();  
        userService.create(admin, admin, userName, password, password);
        site.create(userName,
                    password,
                    "mydomain",
                    siteName, 
                    "my site description", 
                    Visibility.PUBLIC);  
        content.updateDocumentContent(userName, password, siteName, DocumentType.TEXT_PLAIN, "fakeDoc", "new content");    
    }
}
