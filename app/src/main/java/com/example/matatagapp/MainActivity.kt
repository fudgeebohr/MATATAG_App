package com.example.matatagapp

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream

// Define the color palette
val LightColors = lightColorScheme(
    primary = Color(0xFF6200EE),
    primaryContainer = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

// Apply the custom theme
@Composable
fun MATATAGAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, typography = CustomTypography, content = content)
}

// Custom Typography
private val CustomTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    )
)

class MainActivity : ComponentActivity() {

    private val pdfList = listOf(
        PdfFile("CGP Module 2", "CGP_Module-2.pdf"),
        PdfFile("CGP Module 3", "CGP_Module-3.pdf"),
        PdfFile("CGP Module 4", "CGP_Module-4.pdf"),
        PdfFile("CGP Module 7", "CGP_Module-7.pdf")
    )

    private fun PdfFile(Title: String, FileName: String): Any = Unit

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = PdfAdapter(pdfList) { pdfFile ->
            val intent = Intent(this, PdfViewerActivity::class.java)
            intent.putExtra("pdfFileName", pdfFile.filePath) // Send the filename to the PdfViewerActivity
            startActivity(intent)
        }
    }

    class PdfViewerActivity {

    }

    private fun PdfAdapter(
        pdfList: List<Any>,
        any: Any
    ): RecyclerView.Adapter<RecyclerView.ViewHolder>? {
        TODO("Not yet implemented")
    }

    // Function to create a file from URI
    private fun createPdfFileFromUri(uri: Uri): File {
        val fileName = uri.lastPathSegment ?: "temp.pdf"
        val pdfFile = File(filesDir, fileName)
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(pdfFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return pdfFile
    }

    // Function to open PDF using an intent
    fun openPdf(pdfFile: File) {
        val uri = FileProvider.getUriForFile(this, "$packageName.provider", pdfFile)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            flags = Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        startActivity(intent)
    }

    @Composable
    fun PdfManagerScreen() {
        // State to hold the list of PDF files
        val pdfFiles = remember { mutableStateListOf<File>() }

        // Register for file picking result
        val pickPdfLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument(),
            onResult = { uri: Uri? ->
                uri?.let {
                    val pdfFile = createPdfFileFromUri(it)
                    pdfFiles.add(pdfFile) // Add the PDF file to the list
                }
            }
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        // Launch the file picker when FAB is clicked
                        pickPdfLauncher.launch(arrayOf("application/pdf"))
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add PDF")
                }
            }
        ) { innerPadding ->
            PdfList(
                pdfFiles = pdfFiles, // Pass the list of PDF files to the PdfList composable
                modifier = Modifier.padding(innerPadding),
                onPdfClick = { pdfFile ->
                    openPdf(pdfFile)
                }
            )
        }
    }

    @Composable
    fun PdfList(pdfFiles: List<File>, modifier: Modifier = Modifier, onPdfClick: (File) -> Unit) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)  // Adds spacing between items
        ) {
            items(pdfFiles) { pdfFile ->
                PdfListItem(pdfFile = pdfFile, onPdfClick = onPdfClick) // Pass each file to PdfListItem
            }
        }
    }

    @Composable
    fun PdfListItem(pdfFile: File, onPdfClick: (File) -> Unit) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onPdfClick(pdfFile) },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_pdf), // Use your custom vector asset
                    contentDescription = null,
                    tint = Color.Red
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = pdfFile.name, style = MaterialTheme.typography.bodyLarge) // Display the file name
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun PdfManagerScreenPreview() {
        MATATAGAppTheme {
            PdfManagerScreen()
        }
    }
}
