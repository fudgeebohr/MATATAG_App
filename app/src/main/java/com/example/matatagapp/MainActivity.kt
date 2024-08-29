package com.example.matatagapp

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
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

// Define the PdfFile data class
data class PdfFile(val title: String, val fileName: String)

class MainActivity : ComponentActivity() {

    private val pdfList = listOf(
        PdfFile("CGP Module 2", "CGP_Module-2.pdf"),
        PdfFile("CGP Module 3", "CGP_Module-3.pdf"),
        PdfFile("CGP Module 4", "CGP_Module-4.pdf"),
        PdfFile("CGP Module 7", "CGP_Module-7.pdf")
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = PdfAdapter(pdfList) { pdfFile ->
            val intent = Intent(this, PdfViewerActivity::class.java)
            intent.putExtra("pdfFileName", pdfFile.fileName) // Send the filename to the PdfViewerActivity
            startActivity(intent)
        }
    }

    class PdfViewerActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_pdf_viewer)

            val pdfFileName = intent.getStringExtra("pdfFileName")
            if (pdfFileName != null) {
                // Initialize PDF viewer with the file
            } else {
                // Log error and handle appropriately
                Log.e("PdfViewerActivity", "PDF file name is null")
            }
        }
    }



    private fun PdfAdapter(
        pdfList: List<PdfFile>,
        onItemClick: (PdfFile) -> Unit
    ): RecyclerView.Adapter<RecyclerView.ViewHolder> {

        // ViewHolder class to hold the view for each PDF item
        class PdfViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        }

        // Inflate the item layout and create a ViewHolder instance
        fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.pdf_item_layout, parent, false)
            return PdfViewHolder(view)
        }

        // Bind data to the views in the ViewHolder
        fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
            val pdfFile = pdfList[position]
            holder.titleTextView.text = pdfFile.title

            // Handle click event on the PDF item
            holder.itemView.setOnClickListener {
                onItemClick(pdfFile)
            }
        }

        // Return the total number of items
        fun getItemCount(): Int = pdfList.size
        return TODO("Provide the return value")
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
    private fun openPdf(pdfFile: File) {
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
