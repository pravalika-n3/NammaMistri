package com.example.nammamistri

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import org.json.JSONArray
import org.json.JSONObject
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image as ImageIcon
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.ceil
import kotlin.math.roundToInt

data class Site(
    val id: Long,
    val name: String,
    val owner: String,
    val location: String,
    val phone: String,
    val createdDate: String,
)

data class Worker(
    val id: Long,
    val siteId: Long,
    val name: String,
    val phone: String,
    val dailyWage: Double,
    val skill: String,
    val daysPresent: Double,
    val advance: Double,
)

data class MaterialRate(
    val id: Long,
    val material: String,
    val rate: Double,
    val unit: String,
)

data class SitePhoto(
    val id: Long,
    val siteId: Long,
    val uri: String,
    val description: String,
    val date: String,
)

data class CalculationResult(
    val title: String,
    val wallVolume: Double,
    val bricks: Int,
    val bricksWithWastage: Int,
    val cementBags: Double,
    val sandLoads: Double,
)

enum class Screen {
    Home,
    Sites,
    AddSite,
    Detail,
    Dashboard,
    Calculator,
    Labor,
    AddWorker,
    WorkerDetail,
    Photos,
    Rates,
    WagesReport,
}

class LocalStore(context: Context) {
    private val prefs = context.getSharedPreferences("namma_mistri_store", Context.MODE_PRIVATE)

    fun loadSites(): List<Site> = readArray("sites").map {
        Site(
            id = it.getLong("id"),
            name = it.getString("name"),
            owner = it.getString("owner"),
            location = it.getString("location"),
            phone = it.optString("phone"),
            createdDate = it.getString("createdDate"),
        )
    }

    fun loadWorkers(): List<Worker> = readArray("workers").map {
        Worker(
            id = it.getLong("id"),
            siteId = it.getLong("siteId"),
            name = it.getString("name"),
            phone = it.optString("phone"),
            dailyWage = it.getDouble("dailyWage"),
            skill = it.getString("skill"),
            daysPresent = it.getDouble("daysPresent"),
            advance = it.getDouble("advance"),
        )
    }

    fun loadRates(): List<MaterialRate> {
        val stored = readArray("rates").map {
            MaterialRate(
                id = it.getLong("id"),
                material = it.getString("material"),
                rate = it.getDouble("rate"),
                unit = it.getString("unit"),
            )
        }
        return if (stored.isEmpty()) {
            listOf(
                MaterialRate(1, "Bricks", 8.0, "piece"),
                MaterialRate(2, "Cement", 420.0, "bag"),
                MaterialRate(3, "Sand", 6500.0, "load"),
                MaterialRate(4, "Mason labor", 900.0, "day"),
            )
        } else {
            stored
        }
    }

    fun loadPhotos(): List<SitePhoto> = readArray("photos").map {
        SitePhoto(
            id = it.getLong("id"),
            siteId = it.getLong("siteId"),
            uri = it.getString("uri"),
            description = it.optString("description"),
            date = it.getString("date"),
        )
    }

    fun saveSites(items: List<Site>) = writeArray("sites", items.map {
        JSONObject()
            .put("id", it.id)
            .put("name", it.name)
            .put("owner", it.owner)
            .put("location", it.location)
            .put("phone", it.phone)
            .put("createdDate", it.createdDate)
    })

    fun saveWorkers(items: List<Worker>) = writeArray("workers", items.map {
        JSONObject()
            .put("id", it.id)
            .put("siteId", it.siteId)
            .put("name", it.name)
            .put("phone", it.phone)
            .put("dailyWage", it.dailyWage)
            .put("skill", it.skill)
            .put("daysPresent", it.daysPresent)
            .put("advance", it.advance)
    })

    fun saveRates(items: List<MaterialRate>) = writeArray("rates", items.map {
        JSONObject()
            .put("id", it.id)
            .put("material", it.material)
            .put("rate", it.rate)
            .put("unit", it.unit)
    })

    fun savePhotos(items: List<SitePhoto>) = writeArray("photos", items.map {
        JSONObject()
            .put("id", it.id)
            .put("siteId", it.siteId)
            .put("uri", it.uri)
            .put("description", it.description)
            .put("date", it.date)
    })

    private fun readArray(key: String): List<JSONObject> {
        val json = prefs.getString(key, "[]") ?: "[]"
        val array = JSONArray(json)
        val list = mutableListOf<JSONObject>()
        for (i in 0 until array.length()) {
            list.add(array.getJSONObject(i))
        }
        return list
    }

    private fun writeArray(key: String, objects: List<JSONObject>) {
        val array = JSONArray()
        objects.forEach { array.put(it) }
        prefs.edit().putString(key, array.toString()).apply()
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NammaMistriApp()
        }
    }
}

@Composable
fun NammaMistriApp() {
    val context = LocalContext.current
    val store = remember { LocalStore(context) }
    var screen by remember { mutableStateOf(Screen.Home) }
    var selectedSiteId by remember { mutableStateOf<Long?>(null) }
    var selectedWorkerId by remember { mutableStateOf<Long?>(null) }
    var sites by remember { mutableStateOf(store.loadSites()) }
    var workers by remember { mutableStateOf(store.loadWorkers()) }
    var rates by remember { mutableStateOf(store.loadRates()) }
    var photos by remember { mutableStateOf(store.loadPhotos()) }

    LaunchedEffect(sites) { store.saveSites(sites) }
    LaunchedEffect(workers) { store.saveWorkers(workers) }
    LaunchedEffect(rates) { store.saveRates(rates) }
    LaunchedEffect(photos) { store.savePhotos(photos) }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            primary = Color(0xFFE65100),
            secondary = Color(0xFFFFE0B2),
            surface = Color(0xFFFFFBF5),
            background = Color(0xFFFFF3E0),
        )
    ) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            val selectedSite = sites.find { it.id == selectedSiteId }
            val selectedWorker = workers.find { it.id == selectedWorkerId }
            
            when (screen) {
                Screen.Home -> HomeScreen(onGetStarted = { screen = Screen.Sites })
                Screen.Sites -> SitesScreen(
                    sites = sites,
                    workers = workers,
                    photos = photos,
                    onAdd = { screen = Screen.AddSite },
                    onOpen = {
                        selectedSiteId = it.id
                        screen = Screen.Detail
                    },
                    onDashboard = { screen = Screen.Dashboard },
                    onPhotos = {
                        selectedSiteId = it.id
                        screen = Screen.Photos
                    },
                    onWagesReport = {
                        selectedSiteId = it.id
                        screen = Screen.WagesReport
                    },
                    onDelete = { site ->
                        sites = sites.filterNot { it.id == site.id }
                        workers = workers.filterNot { it.siteId == site.id }
                        photos = photos.filterNot { it.siteId == site.id }
                    },
                    onRates = { screen = Screen.Rates },
                )
                Screen.Dashboard -> DashboardScreen(
                    sites = sites,
                    workers = workers,
                    photos = photos,
                    rates = rates,
                    onBack = { screen = Screen.Sites },
                    onSites = { screen = Screen.Sites },
                    onRates = { screen = Screen.Rates },
                )
                Screen.AddSite -> AddSiteScreen(
                    onBack = { screen = Screen.Sites },
                    onSave = { name, owner, location, phone ->
                        val site = Site(nextId(sites.map { it.id }), name, owner, location, phone, today())
                        sites = sites + site
                        selectedSiteId = site.id
                        screen = Screen.Detail
                    },
                )
                Screen.Detail -> {
                    if (selectedSite != null) {
                        DetailScreen(
                            site = selectedSite,
                            onBack = { screen = Screen.Sites },
                            onCalculator = { screen = Screen.Calculator },
                            onLabor = { screen = Screen.Labor },
                            onPhotos = { screen = Screen.Photos },
                            onRates = { screen = Screen.Rates },
                            onWagesReport = { screen = Screen.WagesReport },
                            onDashboard = { screen = Screen.Dashboard },
                        )
                    } else {
                        LaunchedEffect(Unit) { screen = Screen.Sites }
                    }
                }
                Screen.Calculator -> CalculatorScreen(
                    site = selectedSite,
                    onBack = { screen = if (selectedSite == null) Screen.Sites else Screen.Detail },
                )
                Screen.Labor -> {
                    if (selectedSite != null) {
                        LaborScreen(
                            site = selectedSite,
                            workers = workers.filter { it.siteId == selectedSite.id },
                            onBack = { screen = Screen.Detail },
                            onAdd = { screen = Screen.AddWorker },
                            onOpen = {
                                selectedWorkerId = it.id
                                screen = Screen.WorkerDetail
                            },
                        )
                    } else {
                        LaunchedEffect(Unit) { screen = Screen.Sites }
                    }
                }
                Screen.AddWorker -> {
                    if (selectedSite != null) {
                        AddWorkerScreen(
                            onBack = { screen = Screen.Labor },
                            onSave = { name, phone, wage, skill ->
                                workers = workers + Worker(nextId(workers.map { it.id }), selectedSite.id, name, phone, wage, skill, 0.0, 0.0)
                                screen = Screen.Labor
                            },
                        )
                    } else {
                        LaunchedEffect(Unit) { screen = Screen.Sites }
                    }
                }
                Screen.WorkerDetail -> {
                    if (selectedWorker != null) {
                        WorkerDetailScreen(
                            worker = selectedWorker,
                            onBack = { screen = Screen.Labor },
                            onUpdate = { updated ->
                                workers = workers.map { if (it.id == updated.id) updated else it }
                            },
                        )
                    } else {
                        LaunchedEffect(Unit) { screen = Screen.Labor }
                    }
                }
                Screen.Photos -> {
                    if (selectedSite != null) {
                        PhotosScreen(
                            site = selectedSite,
                            photos = photos.filter { it.siteId == selectedSite.id },
                            onBack = { screen = Screen.Detail },
                            onAdd = { uri ->
                                photos = photos + SitePhoto(nextId(photos.map { it.id }), selectedSite.id, uri.toString(), "Progress photo", today())
                            },
                            onDelete = { photo -> photos = photos.filterNot { it.id == photo.id } },
                        )
                    } else {
                        LaunchedEffect(Unit) { screen = Screen.Sites }
                    }
                }
                Screen.Rates -> RatesScreen(
                    rates = rates,
                    onBack = { screen = if (selectedSite == null) Screen.Sites else Screen.Detail },
                    onSave = { item ->
                        rates = if (item.id == 0L) {
                            rates + item.copy(id = nextId(rates.map { it.id }))
                        } else {
                            rates.map { if (it.id == item.id) item else it }
                        }
                    },
                    onDelete = { item -> rates = rates.filterNot { it.id == item.id } },
                )
                Screen.WagesReport -> {
                    if (selectedSite != null) {
                        WagesReportScreen(
                            site = selectedSite,
                            workers = workers.filter { it.siteId == selectedSite.id },
                            onBack = { screen = Screen.Detail },
                            onAddWorker = { screen = Screen.AddWorker },
                        )
                    } else {
                        LaunchedEffect(Unit) { screen = Screen.Sites }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White,
                ),
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
            )
        },
        floatingActionButton = { floatingActionButton?.invoke() },
        bottomBar = { bottomBar?.invoke() },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun HomeScreen(onGetStarted: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE65100))
            .padding(32.dp),
    ) {
        Box(
            Modifier
                .align(Alignment.TopStart)
                .padding(top = 22.dp, start = 12.dp)
                .size(128.dp)
                .clip(RoundedCornerShape(64.dp))
                .background(Color.White.copy(alpha = 0.10f)),
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(top = 160.dp)
                .size(82.dp)
                .clip(RoundedCornerShape(42.dp))
                .background(Color.White.copy(alpha = 0.10f)),
        )
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 150.dp)
                .size(68.dp)
                .clip(RoundedCornerShape(34.dp))
                .background(Color.White.copy(alpha = 0.10f)),
        )
        Box(
            Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 72.dp, end = 12.dp)
                .size(100.dp)
                .clip(RoundedCornerShape(50.dp))
                .background(Color.White.copy(alpha = 0.10f)),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 92.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Construction,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(56.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "Namma\nMistri",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                lineHeight = 42.sp,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Construction Assistant",
                color = Color(0xFFFFE0B2),
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
            )
            Spacer(Modifier.height(18.dp))
            Text(
                "Your digital companion on the construction site. Track materials, workers, wages and progress - all in one place.",
                color = Color(0xFFFFCC80),
                fontSize = 14.sp,
                lineHeight = 22.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            )
            Spacer(Modifier.height(40.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    LandingFeature("Material Calculator", Icons.Default.Calculate, Modifier.weight(1f))
                    LandingFeature("Labor & Wages", Icons.Default.Group, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    LandingFeature("Site Photos", Icons.Default.ImageIcon, Modifier.weight(1f))
                    LandingFeature("Progress Tracking", Icons.Default.Dashboard, Modifier.weight(1f))
                }
            }
        }
        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(56.dp),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFFE65100),
            ),
            shape = RoundedCornerShape(16.dp),
        ) {
            Text("Get Started", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Text(
            "Manage your sites with confidence",
            color = Color(0xFFFFCC80),
            fontSize = 12.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp),
        )
    }
}

@Composable
fun LandingFeature(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Color(0xFFFFE0B2), modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = Color(0xFFFFE0B2), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, lineHeight = 15.sp)
    }
}

@Composable
fun SitesScreen(
    sites: List<Site>,
    workers: List<Worker>,
    photos: List<SitePhoto>,
    onAdd: () -> Unit,
    onOpen: (Site) -> Unit,
    onDashboard: () -> Unit,
    onPhotos: (Site) -> Unit,
    onWagesReport: (Site) -> Unit,
    onDelete: (Site) -> Unit,
    onRates: () -> Unit,
) {
    AppScaffold(
        title = "Your Active Sites",
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAdd, icon = { Icon(Icons.Default.Add, null) }, text = { Text("Add Site") })
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Sites") })
                NavigationBarItem(selected = false, onClick = onDashboard, icon = { Icon(Icons.Default.Dashboard, null) }, label = { Text("Dashboard") })
                NavigationBarItem(selected = false, onClick = onRates, icon = { Icon(Icons.Default.Payments, null) }, label = { Text("Rates") })
            }
        },
    ) {
        if (sites.isEmpty()) {
            EmptyState("No active sites yet", "Add the first construction site to begin.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
                item { SectionTitle("Active Sites") }
                items(sites) { site ->
                    SiteCard(
                        site = site,
                        workerCount = workers.count { it.siteId == site.id },
                        photoCount = photos.count { it.siteId == site.id },
                        onClick = { onOpen(site) },
                        onPhotos = { onPhotos(site) },
                        onWagesReport = { onWagesReport(site) },
                        onDelete = { onDelete(site) },
                    )
                }
                item { Spacer(Modifier.height(88.dp)) }
            }
        }
    }
}

@Composable
fun SiteCard(
    site: Site,
    workerCount: Int,
    photoCount: Int,
    onClick: () -> Unit,
    onPhotos: () -> Unit,
    onWagesReport: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF5)),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Construction, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(site.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Owner: ${site.owner}")
                Text(site.location, color = Color(0xFF6D4C41))
                Text("$workerCount workers | $photoCount photos | ${site.createdDate}", color = Color(0xFF8D6E63), fontSize = 12.sp)
            }
            Box(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE8F5E9))
                    .padding(horizontal = 9.dp, vertical = 4.dp),
            ) {
                Text("active", color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete site")
            }
        }
        Row(
            Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FilledTonalButton(onClick = onPhotos, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.ImageIcon, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Add Photos")
            }
            FilledTonalButton(onClick = onWagesReport, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Payments, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Wages Report")
            }
        }
    }
}

@Composable
fun DashboardScreen(
    sites: List<Site>,
    workers: List<Worker>,
    photos: List<SitePhoto>,
    rates: List<MaterialRate>,
    onBack: () -> Unit,
    onSites: () -> Unit,
    onRates: () -> Unit,
) {
    val totalWages = workers.sumOf { it.daysPresent * it.dailyWage }
    val totalAdvance = workers.sumOf { it.advance }
    val totalBalance = workers.sumOf { balanceDue(it) }

    AppScaffold(
        title = "Dashboard",
        onBack = onBack,
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = false, onClick = onSites, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Sites") })
                NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Dashboard, null) }, label = { Text("Dashboard") })
                NavigationBarItem(selected = false, onClick = onRates, icon = { Icon(Icons.Default.Payments, null) }, label = { Text("Rates") })
            }
        },
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            item { SectionTitle("All Site Summary") }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    SmallMetricCard("Sites", sites.size.toString(), Modifier.weight(1f))
                    SmallMetricCard("Workers", workers.size.toString(), Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    SmallMetricCard("Photos", photos.size.toString(), Modifier.weight(1f))
                    SmallMetricCard("Rates", rates.size.toString(), Modifier.weight(1f))
                }
            }
            item { MetricCard("Wages Earned", "Rs. ${formatNumber(totalWages)}", "Total amount calculated from attendance") }
            item { MetricCard("Advance Paid", "Rs. ${formatNumber(totalAdvance)}", "Cash already paid to workers") }
            item { MetricCard("Balance Due", "Rs. ${formatNumber(totalBalance)}", "Pending amount across all active sites") }
            item { SectionTitle("Active Sites") }
            if (sites.isEmpty()) {
                item { EmptyState("No active sites yet", "Add a site to see dashboard totals.") }
            } else {
                items(sites) { site ->
                    val siteWorkers = workers.filter { it.siteId == site.id }
                    MetricCard(
                        site.name,
                        "Rs. ${formatNumber(siteWorkers.sumOf { balanceDue(it) })}",
                        "${siteWorkers.size} workers | ${photos.count { it.siteId == site.id }} photos",
                    )
                }
            }
            item { Spacer(Modifier.height(88.dp)) }
        }
    }
}

@Composable
fun SmallMetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = modifier,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = Color(0xFF4D5A50), fontWeight = FontWeight.SemiBold)
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AddSiteScreen(onBack: () -> Unit, onSave: (String, String, String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var owner by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    AppScaffold(title = "Add New Site", onBack = onBack) {
        FormColumn {
            AppTextField("Site Name", name) { name = it }
            AppTextField("Owner Name", owner) { owner = it }
            AppTextField("Location", location) { location = it }
            AppTextField("Phone Number", phone, KeyboardType.Phone) { phone = it }
            if (showError) Text("Site name, owner, and location are required.", color = MaterialTheme.colorScheme.error)
            Button(
                onClick = {
                    if (name.isBlank() || owner.isBlank() || location.isBlank()) showError = true
                    else onSave(name.trim(), owner.trim(), location.trim(), phone.trim())
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("Save Site")
            }
        }
    }
}

@Composable
fun DetailScreen(
    site: Site,
    onBack: () -> Unit,
    onCalculator: () -> Unit,
    onLabor: () -> Unit,
    onPhotos: () -> Unit,
    onRates: () -> Unit,
    onWagesReport: () -> Unit,
    onDashboard: () -> Unit,
) {
    AppScaffold(title = site.name, onBack = onBack) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxSize()) {
            item {
                Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(site.name, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        Text("Owner: ${site.owner}")
                        Text(site.location)
                        if (site.phone.isNotBlank()) Text("Phone: ${site.phone}")
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        FeatureButton("Material Calculator", Icons.Default.Calculate, Modifier.weight(1f), onCalculator)
                        FeatureButton("Labor Diary", Icons.Default.Group, Modifier.weight(1f), onLabor)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        FeatureButton("Site Photos", Icons.Default.ImageIcon, Modifier.weight(1f), onPhotos)
                        FeatureButton("Standard Rates", Icons.Default.Payments, Modifier.weight(1f), onRates)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        FeatureButton("Wages Report", Icons.Default.Payments, Modifier.weight(1f), onWagesReport)
                        FeatureButton("Dashboard", Icons.Default.Dashboard, Modifier.weight(1f), onDashboard)
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(128.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(Modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp))
            Spacer(Modifier.height(10.dp))
            Text(label, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun CalculatorScreen(site: Site?, onBack: () -> Unit) {
    var type by remember { mutableStateOf("Room") }
    var length by remember { mutableStateOf("") }
    var width by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var thickness by remember { mutableStateOf(0.75) }
    var result by remember { mutableStateOf<CalculationResult?>(null) }

    AppScaffold(title = "Material Calculator", onBack = onBack) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxSize()) {
            item { 
                if (site != null) {
                    Text(site.name, color = Color(0xFF4D5A50))
                }
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = type == "Wall", onClick = { type = "Wall" })
                    Text("Single Wall")
                    Spacer(Modifier.width(16.dp))
                    RadioButton(selected = type == "Room", onClick = { type = "Room" })
                    Text("Full Room")
                }
            }
            item { AppTextField("Length in feet", length, KeyboardType.Decimal) { length = it } }
            if (type == "Room") {
                item { AppTextField("Width in feet", width, KeyboardType.Decimal) { width = it } }
            }
            item { AppTextField("Height in feet", height, KeyboardType.Decimal) { height = it } }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = thickness == 0.375, onClick = { thickness = 0.375 })
                    Text("4.5 inch")
                    Spacer(Modifier.width(16.dp))
                    RadioButton(selected = thickness == 0.75, onClick = { thickness = 0.75 })
                    Text("9 inch")
                }
            }
            item {
                Button(
                    onClick = {
                        val l = length.toDoubleOrNull() ?: 0.0
                        val w = if (type == "Room") width.toDoubleOrNull() ?: 0.0 else 0.0
                        val h = height.toDoubleOrNull() ?: 0.0
                        if (l > 0 && h > 0 && (type == "Wall" || w > 0)) {
                            result = calculateMaterials(type, l, w, h, thickness)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Calculate, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Calculate")
                }
            }
            item {
                val res = result
                if (res != null) {
                    ResultCards(res)
                }
            }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun ResultCards(result: CalculationResult) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(result.title)
        MetricCard("Bricks", "${result.bricks} pieces", "Order about ${result.bricksWithWastage} with 5 percent wastage")
        MetricCard("Cement", "${formatNumber(result.cementBags)} bags", "50 kg bags, 1:6 mortar ratio")
        MetricCard("Sand", "${formatNumber(result.sandLoads)} loads", "1 load treated as 100 cft")
        MetricCard("Wall Volume", "${formatNumber(result.wallVolume)} cft", "Calculated from entered dimensions")
    }
}

@Composable
fun LaborScreen(site: Site, workers: List<Worker>, onBack: () -> Unit, onAdd: () -> Unit, onOpen: (Worker) -> Unit) {
    val totalBalance = workers.sumOf { balanceDue(it) }
    AppScaffold(
        title = "Labor Diary",
        onBack = onBack,
        floatingActionButton = { ExtendedFloatingActionButton(onClick = onAdd, icon = { Icon(Icons.Default.Add, null) }, text = { Text("Add Worker") }) },
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            item { Text(site.name, color = Color(0xFF4D5A50)) }
            if (workers.isEmpty()) {
                item { EmptyState("No workers yet", "Add masons, helpers, and other labor here.") }
            } else {
                items(workers) { worker ->
                    WorkerCard(worker, onClick = { onOpen(worker) })
                }
                item { MetricCard("Total Balance", "Rs. ${formatNumber(totalBalance)}", "Amount due across all workers") }
                item { Spacer(Modifier.height(88.dp)) }
            }
        }
    }
}

@Composable
fun WorkerCard(worker: Worker, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(worker.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("${worker.skill} | Rs. ${formatNumber(worker.dailyWage)} per day")
            Text("Days: ${formatNumber(worker.daysPresent)} | Advance: Rs. ${formatNumber(worker.advance)}")
            Text("Balance Due: Rs. ${formatNumber(balanceDue(worker))}", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun AddWorkerScreen(onBack: () -> Unit, onSave: (String, String, Double, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var wage by remember { mutableStateOf("") }
    var skill by remember { mutableStateOf("Mason") }
    var showError by remember { mutableStateOf(false) }
    val skills = listOf("Mason", "Helper", "Carpenter", "Painter")
    AppScaffold(title = "Add Worker", onBack = onBack) {
        FormColumn {
            AppTextField("Worker Name", name) { name = it }
            AppTextField("Phone Number", phone, KeyboardType.Phone) { phone = it }
            AppTextField("Daily Wage", wage, KeyboardType.Decimal) { wage = it }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                for (option in skills) {
                    FilledTonalButton(onClick = { skill = option }) {
                        Text(if (skill == option) "$option *" else option)
                    }
                }
            }
            if (showError) Text("Worker name and daily wage are required.", color = MaterialTheme.colorScheme.error)
            Button(
                onClick = {
                    val parsedWage = wage.toDoubleOrNull() ?: 0.0
                    if (name.isBlank() || parsedWage <= 0) showError = true else onSave(name.trim(), phone.trim(), parsedWage, skill)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Save, null)
                Spacer(Modifier.width(8.dp))
                Text("Save Worker")
            }
        }
    }
}

@Composable
fun WorkerDetailScreen(worker: Worker, onBack: () -> Unit, onUpdate: (Worker) -> Unit) {
    var advance by remember { mutableStateOf("") }
    AppScaffold(title = worker.name, onBack = onBack) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            item {
                MetricCard("Balance Due", "Rs. ${formatNumber(balanceDue(worker))}", "${worker.skill} | Rs. ${formatNumber(worker.dailyWage)} per day")
            }
            item {
                Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Mark Attendance", fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            FilledTonalButton(onClick = { onUpdate(worker.copy(daysPresent = worker.daysPresent + 1.0)) }) { Text("Present") }
                            FilledTonalButton(onClick = { onUpdate(worker.copy(daysPresent = worker.daysPresent + 0.5)) }) { Text("Half Day") }
                            FilledTonalButton(onClick = {}) { Text("Absent") }
                        }
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Pay Advance", fontWeight = FontWeight.Bold)
                        AppTextField("Amount", advance, KeyboardType.Decimal) { advance = it }
                        Button(
                            onClick = {
                                val amount = advance.toDoubleOrNull() ?: 0.0
                                if (amount > 0) {
                                    onUpdate(worker.copy(advance = worker.advance + amount))
                                    advance = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("Add Advance")
                        }
                    }
                }
            }
            item {
                MetricCard("Attendance Summary", "${formatNumber(worker.daysPresent)} days", "Advance paid: Rs. ${formatNumber(worker.advance)}")
            }
        }
    }
}

@Composable
fun PhotosScreen(site: Site, photos: List<SitePhoto>, onBack: () -> Unit, onAdd: (Uri) -> Unit, onDelete: (SitePhoto) -> Unit) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            onAdd(uri)
        }
    }
    AppScaffold(
        title = "Site Photos",
        onBack = onBack,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { picker.launch(arrayOf("image/*")) },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Add Photo") },
            )
        },
    ) {
        if (photos.isEmpty()) {
            EmptyState("No photos yet", "Add progress photos from the device gallery.")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(photos) { photo ->
                    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column {
                            Image(
                                painter = rememberAsyncImagePainter(photo.uri),
                                contentDescription = photo.description,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                            )
                            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(site.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(photo.date, fontSize = 12.sp, color = Color(0xFF6F786F))
                                }
                                IconButton(onClick = { onDelete(photo) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete photo")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WagesReportScreen(site: Site, workers: List<Worker>, onBack: () -> Unit, onAddWorker: () -> Unit) {
    val totalDays = workers.sumOf { it.daysPresent }
    val totalWages = workers.sumOf { it.daysPresent * it.dailyWage }
    val totalAdvance = workers.sumOf { it.advance }
    val totalBalance = workers.sumOf { balanceDue(it) }

    AppScaffold(
        title = "Wages Report",
        onBack = onBack,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddWorker,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Add Worker") },
            )
        },
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            item {
                Text(site.name, color = Color(0xFF4D5A50), fontWeight = FontWeight.SemiBold)
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    SmallMetricCard("Workers", workers.size.toString(), Modifier.weight(1f))
                    SmallMetricCard("Days", formatNumber(totalDays), Modifier.weight(1f))
                }
            }
            item { MetricCard("Total Wages", "Rs. ${formatNumber(totalWages)}", "Days present multiplied by daily wage") }
            item { MetricCard("Advance Paid", "Rs. ${formatNumber(totalAdvance)}", "Amount already paid at this site") }
            item { MetricCard("Balance Due", "Rs. ${formatNumber(totalBalance)}", "Amount still pending for workers") }
            item { SectionTitle("Worker Details") }
            if (workers.isEmpty()) {
                item { EmptyState("No wages yet", "Add workers and mark attendance to build the report.") }
            } else {
                items(workers) { worker ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(worker.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("${worker.skill} | Rs. ${formatNumber(worker.dailyWage)} per day")
                            Text("Days: ${formatNumber(worker.daysPresent)} | Wage: Rs. ${formatNumber(worker.daysPresent * worker.dailyWage)}")
                            Text("Advance: Rs. ${formatNumber(worker.advance)} | Balance: Rs. ${formatNumber(balanceDue(worker))}", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(88.dp)) }
        }
    }
}

@Composable
fun RatesScreen(rates: List<MaterialRate>, onBack: () -> Unit, onSave: (MaterialRate) -> Unit, onDelete: (MaterialRate) -> Unit) {
    var editing by remember { mutableStateOf<MaterialRate?>(null) }
    AppScaffold(
        title = "Standard Rates",
        onBack = onBack,
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { editing = MaterialRate(0, "", 0.0, "") }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("Add Rate") })
        },
    ) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
            items(rates) { rate ->
                Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f).clickable { editing = rate }) {
                            Text(rate.material, fontWeight = FontWeight.Bold)
                            Text("Rs. ${formatNumber(rate.rate)} per ${rate.unit}")
                        }
                        IconButton(onClick = { onDelete(rate) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete rate")
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(88.dp)) }
        }
    }
    val currentEditing = editing
    if (currentEditing != null) {
        RateDialog(
            initial = currentEditing,
            onDismiss = { editing = null },
            onSave = {
                onSave(it)
                editing = null
            },
        )
    }
}

@Composable
fun RateDialog(initial: MaterialRate, onDismiss: () -> Unit, onSave: (MaterialRate) -> Unit) {
    var material by remember { mutableStateOf(initial.material) }
    var rate by remember { mutableStateOf(if (initial.rate == 0.0) "" else initial.rate.toString()) }
    var unit by remember { mutableStateOf(initial.unit) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial.id == 0L) "Add Rate" else "Edit Rate") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTextField("Material", material) { material = it }
                AppTextField("Rate", rate, KeyboardType.Decimal) { rate = it }
                AppTextField("Unit", unit) { unit = it }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsed = rate.toDoubleOrNull() ?: 0.0
                    if (material.isNotBlank() && unit.isNotBlank() && parsed > 0) {
                        onSave(initial.copy(material = material.trim(), rate = parsed, unit = unit.trim()))
                    }
                },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
fun FormColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content,
    )
}

@Composable
fun AppTextField(label: String, value: String, keyboardType: KeyboardType = KeyboardType.Text, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp))
}

@Composable
fun MetricCard(title: String, value: String, note: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, color = Color(0xFF4D5A50), fontWeight = FontWeight.SemiBold)
            Text(value, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(note, fontSize = 13.sp, color = Color(0xFF6F786F))
        }
    }
}

@Composable
fun EmptyState(title: String, body: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE8EFE8)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Construction, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(38.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(body, color = Color(0xFF667064), modifier = Modifier.padding(top = 4.dp))
    }
}

fun calculateMaterials(type: String, length: Double, width: Double, height: Double, thickness: Double): CalculationResult {
    val volume = if (type == "Wall") {
        length * height * thickness
    } else {
        2 * (length + width) * height * thickness
    }
    val bricks = (volume * 13.5).roundToInt()
    val mortarVolume = volume * 0.30
    val cementBags = mortarVolume * 0.45 / 1.25
    val sandLoads = mortarVolume * (6.0 / 7.0) * 1.54 / 100.0
    val thicknessLabel = if (thickness == 0.375) "4.5 inch" else "9 inch"
    val title = if (type == "Wall") "Wall: ${formatNumber(length)} x ${formatNumber(height)} ft, $thicknessLabel" else "Room: ${formatNumber(length)} x ${formatNumber(width)} x ${formatNumber(height)} ft"
    return CalculationResult(
        title = title,
        wallVolume = volume,
        bricks = bricks,
        bricksWithWastage = ceil(bricks * 1.05).toInt(),
        cementBags = cementBags,
        sandLoads = sandLoads,
    )
}

fun balanceDue(worker: Worker): Double = worker.daysPresent * worker.dailyWage - worker.advance

fun nextId(ids: List<Long>): Long = (ids.maxOrNull() ?: 0L) + 1L

fun today(): String = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))

fun formatNumber(value: Double): String {
    val rounded = (value * 10).roundToInt() / 10.0
    return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
}
