package com.guyko.mcp

import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.io.IOException
import java.lang.reflect.Method

class FileCollisionTest {

    @Test
    fun testUniqueFileNameGeneration() {
        // Create a temporary directory for testing
        val tempDir = createTempDir("midimcp_test")
        
        try {
            // Get the private method using reflection
            val mcpServer = MCPServer()
            val method = MCPServer::class.java.getDeclaredMethod("getUniqueFileName", File::class.java, String::class.java, String::class.java)
            method.isAccessible = true
            
            // Test case 1: No existing file
            val file1 = method.invoke(mcpServer, tempDir, "test_preset", "pgm90") as File
            assertEquals("test_preset.pgm90", file1.name)
            assertFalse("File should not exist yet", file1.exists())
            
            // Create the first file
            file1.createNewFile()
            assertTrue("File should now exist", file1.exists())
            
            // Test case 2: File exists, should get (1) suffix
            val file2 = method.invoke(mcpServer, tempDir, "test_preset", "pgm90") as File
            assertEquals("test_preset (1).pgm90", file2.name)
            assertFalse("File with (1) suffix should not exist yet", file2.exists())
            
            // Create the second file
            file2.createNewFile()
            assertTrue("File with (1) suffix should now exist", file2.exists())
            
            // Test case 3: Both files exist, should get (2) suffix
            val file3 = method.invoke(mcpServer, tempDir, "test_preset", "pgm90") as File
            assertEquals("test_preset (2).pgm90", file3.name)
            assertFalse("File with (2) suffix should not exist yet", file3.exists())
            
            // Test case 4: Different base name should not conflict
            val file4 = method.invoke(mcpServer, tempDir, "different_preset", "pgm90") as File
            assertEquals("different_preset.pgm90", file4.name)
            assertFalse("Different base name should not conflict", file4.exists())
            
            // Test case 5: Different extension should not conflict
            val file5 = method.invoke(mcpServer, tempDir, "test_preset", "txt") as File
            assertEquals("test_preset.txt", file5.name)
            assertFalse("Different extension should not conflict", file5.exists())
            
        } finally {
            // Clean up temporary directory
            tempDir.deleteRecursively()
        }
    }
    
    @Test
    fun testFileCollisionWithManyFiles() {
        val tempDir = createTempDir("midimcp_collision_test")
        
        try {
            val mcpServer = MCPServer()
            val method = MCPServer::class.java.getDeclaredMethod("getUniqueFileName", File::class.java, String::class.java, String::class.java)
            method.isAccessible = true
            
            // Create 10 files with same base name
            val createdFiles = mutableListOf<File>()
            
            for (i in 0..9) {
                val file = method.invoke(mcpServer, tempDir, "batch_preset", "pgm90") as File
                file.createNewFile()
                createdFiles.add(file)
            }
            
            // Verify the file names are correct
            assertEquals("batch_preset.pgm90", createdFiles[0].name)
            assertEquals("batch_preset (1).pgm90", createdFiles[1].name)
            assertEquals("batch_preset (2).pgm90", createdFiles[2].name)
            assertEquals("batch_preset (9).pgm90", createdFiles[9].name)
            
            // Verify all files exist
            createdFiles.forEach { file ->
                assertTrue("File ${file.name} should exist", file.exists())
            }
            
            // Test that the next file gets (10) suffix
            val nextFile = method.invoke(mcpServer, tempDir, "batch_preset", "pgm90") as File
            assertEquals("batch_preset (10).pgm90", nextFile.name)
            
        } finally {
            tempDir.deleteRecursively()
        }
    }
}