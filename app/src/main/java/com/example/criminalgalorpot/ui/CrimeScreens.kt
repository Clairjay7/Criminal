package com.example.criminalgalorpot.ui

import android.app.DatePickerDialog
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.criminalgalorpot.model.Crime
import com.example.criminalgalorpot.model.CrimeRepository
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun CriminalIntentApp() {

    var selectedCrimeIdString by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedCrimeId = selectedCrimeIdString?.let { runCatching { UUID.fromString(it) }.getOrNull() }

    Surface(modifier = Modifier.fillMaxSize()) {
        if (selectedCrimeId == null) {
            CrimeListScreen(
                crimes = CrimeRepository.getCrimes(),
                onCrimeClick = { selectedCrimeIdString = it.id.toString() },
                onAddCrime = {
                    val newCrime = Crime(title = "New Crime")
                    CrimeRepository.addCrime(newCrime)
                    selectedCrimeIdString = newCrime.id.toString()
                }
            )
        } else {
            val crime = remember(selectedCrimeId) {
                CrimeRepository.getCrime(selectedCrimeId)
            }
            if (crime != null) {
                CrimeDetailScreen(
                    crime = crime,
                    onBack = { selectedCrimeIdString = null }
                )
            } else {
                // If something goes wrong, go back to list
                selectedCrimeIdString = null
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrimeListScreen(
    crimes: List<Crime>,
    onCrimeClick: (Crime) -> Unit,
    onAddCrime: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("CriminalIntent") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddCrime) {
                Text("+")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            items(crimes, key = { it.id }) { crime ->
                CrimeListItem(crime = crime, onClick = { onCrimeClick(crime) })
            }
        }
    }
}

@Composable
fun CrimeListItem(
    crime: Crime,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = crime.title.ifBlank { "(No title)" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            if (crime.photoPath != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(crime.photoPath),
                    contentDescription = "Crime photo",
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Crop
                )
            }
            if (crime.isSolved) {
                Text(
                    text = "Solved",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = dateFormat.format(crime.date),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrimeDetailScreen(
    crime: Crime,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf(crime.title) }
    var isSolved by remember { mutableStateOf(crime.isSolved) }
    var date by remember { mutableStateOf(crime.date) }
    var suspect by remember { mutableStateOf(crime.suspect ?: "") }
    var photoAttached by remember { mutableStateOf(crime.photoPath != null) }

    val context = LocalContext.current

    val dateFormat = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()) }

    val calendar = remember(date) {
        Calendar.getInstance().apply { time = date }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            crime.photoPath = uri.toString()
            photoAttached = true
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Crime Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<")
                    }
                },
                actions = {
                    TextButton(onClick = onBack) {
                        Text("Save")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        crime.title = it
                    },
                    label = { Text("Crime title") },
                    modifier = Modifier.weight(1f)
                )

                if (crime.photoPath != null) {
                    Image(
                        painter = rememberAsyncImagePainter(crime.photoPath),
                        contentDescription = "Crime photo",
                        modifier = Modifier.size(64.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text("Date", style = MaterialTheme.typography.labelSmall)
                    Text(dateFormat.format(date))
                }
                Button(onClick = {
                    val listener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        calendar.set(Calendar.YEAR, year)
                        calendar.set(Calendar.MONTH, month)
                        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                        val newDate = calendar.time
                        date = newDate
                        crime.date = newDate
                    }
                    DatePickerDialog(
                        context,
                        listener,
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }) {
                    Text("Change Date")
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Solved")
                Switch(
                    checked = isSolved,
                    onCheckedChange = {
                        isSolved = it
                        crime.isSolved = it
                    }
                )
            }

            OutlinedTextField(
                value = suspect,
                onValueChange = {
                    suspect = it
                    crime.suspect = it.ifBlank { null }
                },
                label = { Text("Suspect (from contacts later)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    imagePickerLauncher.launch("image/*")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (photoAttached) "Change Photo" else "Attach Photo")
            }

            Button(
                onClick = {
                    val solvedString = if (crime.isSolved) "Solved" else "Unsolved"
                    val suspectString = crime.suspect ?: "No suspect"
                    val report = buildString {
                        appendLine("Crime: ${crime.title.ifBlank { "(No title)" }}")
                        appendLine("Date: ${dateFormat.format(crime.date)}")
                        appendLine("Status: $solvedString")
                        appendLine("Suspect: $suspectString")
                    }

                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Crime Report")
                        putExtra(Intent.EXTRA_TEXT, report)
                    }

                    val chooser = Intent.createChooser(sendIntent, "Share crime report with")
                    context.startActivity(chooser)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Share Report")
            }
        }
    }
}
