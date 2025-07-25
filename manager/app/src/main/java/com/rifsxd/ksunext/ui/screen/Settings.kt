package com.rifsxd.ksunext.ui.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.IconSource
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.list.ListDialog
import com.maxkeppeler.sheets.list.models.ListOption
import com.maxkeppeler.sheets.list.models.ListSelection
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.destinations.AppProfileTemplateScreenDestination
import com.ramcosta.composedestinations.generated.destinations.FlashScreenDestination
import com.ramcosta.composedestinations.generated.destinations.BackupRestoreScreenDestination
import com.ramcosta.composedestinations.generated.destinations.CustomizationScreenDestination
import com.ramcosta.composedestinations.generated.destinations.DeveloperScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.rifsxd.ksunext.BuildConfig
import com.rifsxd.ksunext.Natives
import com.rifsxd.ksunext.ksuApp
import com.rifsxd.ksunext.R
import com.rifsxd.ksunext.ui.component.AboutDialog
import com.rifsxd.ksunext.ui.component.ConfirmResult
import com.rifsxd.ksunext.ui.component.DialogHandle
import com.rifsxd.ksunext.ui.component.SwitchItem
import com.rifsxd.ksunext.ui.component.rememberConfirmDialog
import com.rifsxd.ksunext.ui.component.rememberCustomDialog
import com.rifsxd.ksunext.ui.component.rememberLoadingDialog
import com.rifsxd.ksunext.ui.util.LocalSnackbarHost
import com.rifsxd.ksunext.ui.util.getBugreportFile
import com.rifsxd.ksunext.ui.util.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * @author weishu
 * @date 2023/1/1.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Destination<RootGraph>
@Composable
fun SettingScreen(navigator: DestinationsNavigator) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val snackBarHost = LocalSnackbarHost.current

    val isManager = Natives.becomeManager(ksuApp.packageName)
    val ksuVersion = if (isManager) Natives.version else null

    Scaffold(
        topBar = {
            TopBar(
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackBarHost) },
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
    ) { paddingValues ->
        val aboutDialog = rememberCustomDialog {
            AboutDialog(it)
        }
        val loadingDialog = rememberLoadingDialog()
        val shrinkDialog = rememberConfirmDialog()

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
        ) {

            val context = LocalContext.current
            val scope = rememberCoroutineScope()

            val exportBugreportLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.CreateDocument("application/gzip")
            ) { uri: Uri? ->
                if (uri == null) return@rememberLauncherForActivityResult
                scope.launch(Dispatchers.IO) {
                    loadingDialog.show()
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        getBugreportFile(context).inputStream().use {
                            it.copyTo(output)
                        }
                    }
                    loadingDialog.hide()
                    snackBarHost.showSnackbar(context.getString(R.string.log_saved))
                }
            }

            val profileTemplate = stringResource(id = R.string.settings_profile_template)
            if (ksuVersion != null) {
                ListItem(
                    leadingContent = { Icon(Icons.Filled.Fence, profileTemplate) },
                    headlineContent = { Text(
                        text = profileTemplate,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    ) },
                    supportingContent = { Text(stringResource(id = R.string.settings_profile_template_summary)) },
                    modifier = Modifier.clickable {
                        navigator.navigate(AppProfileTemplateScreenDestination)
                    }
                )
            }

            var umountChecked by rememberSaveable {
                mutableStateOf(Natives.isDefaultUmountModules())
            }
            if (ksuVersion != null) {
                SwitchItem(
                    icon = Icons.Filled.FolderDelete,
                    title = stringResource(id = R.string.settings_umount_modules_default),
                    summary = stringResource(id = R.string.settings_umount_modules_default_summary),
                    checked = umountChecked

                ) {
                    if (Natives.setDefaultUmountModules(it)) {
                        umountChecked = it
                    }
                }
            }

            if (ksuVersion != null) {
                if (Natives.version >= Natives.MINIMAL_SUPPORTED_SU_COMPAT) {
                    var isSuDisabled by rememberSaveable {
                        mutableStateOf(!Natives.isSuEnabled())
                    }
                    SwitchItem(
                        icon = Icons.Filled.RemoveModerator,
                        title = stringResource(id = R.string.settings_disable_su),
                        summary = stringResource(id = R.string.settings_disable_su_summary),
                        checked = isSuDisabled
                    ) { checked ->
                        val shouldEnable = !checked
                        if (Natives.setSuEnabled(shouldEnable)) {
                            isSuDisabled = !shouldEnable
                        }
                    }
                }
            }

            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

            val suSFS = getSuSFS()
            val isSUS_SU = getSuSFSFeatures()
            if (suSFS == "Supported") {
                if (isSUS_SU == "CONFIG_KSU_SUSFS_SUS_SU") {
                    var isEnabled by rememberSaveable {
                        mutableStateOf(susfsSUS_SU_Mode() == "2")
                    }

                    LaunchedEffect(Unit) {
                        isEnabled = susfsSUS_SU_Mode() == "2"
                    }

                    SwitchItem(
                        icon = Icons.Filled.VisibilityOff,
                        title = stringResource(id = R.string.settings_susfs_toggle),
                        summary = stringResource(id = R.string.settings_susfs_toggle_summary),
                        checked = isEnabled
                    ) {
                        if (it) {
                            susfsSUS_SU_2()
                        } else {
                            susfsSUS_SU_0()
                        }
                        prefs.edit().putBoolean("enable_sus_su", it).apply()
                        isEnabled = it
                    }
                }
            }

            var useOverlayFs by rememberSaveable {
                mutableStateOf(readMountSystemFile())
            }

            LaunchedEffect(Unit) {
                useOverlayFs = readMountSystemFile()
            }

            var showRebootDialog by remember { mutableStateOf(false) }

            val isOverlayAvailable = overlayFsAvailable()

            if (ksuVersion != null && isOverlayAvailable) {
                SwitchItem(
                    icon = Icons.Filled.Build,
                    title = stringResource(id = R.string.use_overlay_fs),
                    summary = stringResource(id = R.string.use_overlay_fs_summary),
                    checked = useOverlayFs
                ) {
                    prefs.edit().putBoolean("use_overlay_fs", it).apply()
                    useOverlayFs = it
                    if (useOverlayFs) {
                        moduleBackup()
                        updateMountSystemFile(true)
                    } else {
                        moduleMigration()
                        updateMountSystemFile(false)
                    }
                    if (isManager) install()
                    showRebootDialog = true
                }
            }

            if (showRebootDialog) {
                AlertDialog(
                    onDismissRequest = { showRebootDialog = false },
                    title = { Text(
                        text = stringResource(R.string.reboot_required),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    ) },
                    text = { Text(stringResource(R.string.reboot_message)) },
                    confirmButton = {
                        TextButton(onClick = {
                            showRebootDialog = false
                            reboot()
                        }) {
                            Text(stringResource(R.string.reboot))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRebootDialog = false }) {
                            Text(stringResource(R.string.later))
                        }
                    }
                )
            }


            var checkUpdate by rememberSaveable {
                mutableStateOf(
                    prefs.getBoolean("check_update", false)
                )
            }
            SwitchItem(
                icon = Icons.Filled.Update,
                title = stringResource(id = R.string.settings_check_update),
                summary = stringResource(id = R.string.settings_check_update_summary),
                checked = checkUpdate
            ) {
                prefs.edit().putBoolean("check_update", it).apply()
                checkUpdate = it
            }

            if (isOverlayAvailable && useOverlayFs) {
                val shrink = stringResource(id = R.string.shrink_sparse_image)
                val shrinkMessage = stringResource(id = R.string.shrink_sparse_image_message)
                ListItem(
                    leadingContent = {
                        Icon(
                            Icons.Filled.Compress,
                            shrink
                        )
                    },
                    headlineContent = { Text(
                        text = shrink,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    ) },
                    modifier = Modifier.clickable {
                        scope.launch {
                            val result = shrinkDialog.awaitConfirm(title = shrink, content = shrinkMessage)
                            if (result == ConfirmResult.Confirmed) {
                                loadingDialog.withLoading {
                                    shrinkModules()
                                }
                            }
                        }
                    }
                )
            }

            val customization = stringResource(id = R.string.customization)
            ListItem(
                leadingContent = {
                    Icon(
                        Icons.Filled.Palette,
                        customization
                    )
                },
                headlineContent = { Text(
                    text = customization,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                ) },
                modifier = Modifier.clickable {
                    navigator.navigate(CustomizationScreenDestination)
                }
            )

            if (ksuVersion != null) {
                val backupRestore = stringResource(id = R.string.backup_restore)
                ListItem(
                    leadingContent = {
                        Icon(
                            Icons.Filled.Backup,
                            backupRestore
                        )
                    },
                    headlineContent = { Text(
                        text = backupRestore,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    ) },
                    modifier = Modifier.clickable {
                        navigator.navigate(BackupRestoreScreenDestination)
                    }
                )
            }

            val developer = stringResource(id = R.string.developer)
            if (ksuVersion != null) {
                ListItem(
                    leadingContent = {
                        Icon(
                            Icons.Filled.DeveloperBoard,
                            developer
                        )
                    },
                    headlineContent = { Text(
                        text = developer,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    ) },
                    modifier = Modifier.clickable {
                        navigator.navigate(DeveloperScreenDestination)
                    }
                )
            }

            val lkmMode = Natives.version >= Natives.MINIMAL_SUPPORTED_KERNEL_LKM && Natives.isLkmMode
            if (lkmMode) {
                UninstallItem(navigator) {
                    loadingDialog.withLoading(it)
                }
            }

            var showBottomsheet by remember { mutableStateOf(false) }

            ListItem(
                leadingContent = {
                    Icon(
                        Icons.Filled.BugReport,
                        stringResource(id = R.string.export_log)
                    )
                },
                headlineContent = { Text(
                    text = stringResource(id = R.string.export_log),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                ) },
                modifier = Modifier.clickable {
                    showBottomsheet = true
                }
            )
            if (showBottomsheet) {
                ModalBottomSheet(
                    onDismissRequest = { showBottomsheet = false },
                    content = {
                        Row(
                            modifier = Modifier
                                .padding(10.dp)
                                .align(Alignment.CenterHorizontally)

                        ) {
                            Box {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .clickable {
                                            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm")
                                            val current = LocalDateTime.now().format(formatter)
                                            exportBugreportLauncher.launch("KernelSU_Next_bugreport_${current}.tar.gz")
                                            showBottomsheet = false
                                        }
                                ) {
                                    Icon(
                                        Icons.Filled.Save,
                                        contentDescription = null,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                    Text(
                                        text = stringResource(id = R.string.save_log),
                                        modifier = Modifier.padding(top = 16.dp),
                                        textAlign = TextAlign.Center.also {
                                            LineHeightStyle(
                                                alignment = LineHeightStyle.Alignment.Center,
                                                trim = LineHeightStyle.Trim.None
                                            )
                                        }

                                    )
                                }
                            }
                            Box {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .clickable {
                                            scope.launch {
                                                val bugreport = loadingDialog.withLoading {
                                                    withContext(Dispatchers.IO) {
                                                        getBugreportFile(context)
                                                    }
                                                }

                                                val uri: Uri =
                                                    FileProvider.getUriForFile(
                                                        context,
                                                        "${BuildConfig.APPLICATION_ID}.fileprovider",
                                                        bugreport
                                                    )

                                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                    putExtra(Intent.EXTRA_STREAM, uri)
                                                    setDataAndType(uri, "application/gzip")
                                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }

                                                context.startActivity(
                                                    Intent.createChooser(
                                                        shareIntent,
                                                        context.getString(R.string.send_log)
                                                    )
                                                )
                                            }
                                        }
                                ) {
                                    Icon(
                                        Icons.Filled.Share,
                                        contentDescription = null,
                                        modifier = Modifier.align(Alignment.CenterHorizontally)
                                    )
                                    Text(
                                        text = stringResource(id = R.string.send_log),
                                        modifier = Modifier.padding(top = 16.dp),
                                        textAlign = TextAlign.Center.also {
                                            LineHeightStyle(
                                                alignment = LineHeightStyle.Alignment.Center,
                                                trim = LineHeightStyle.Trim.None
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }

            val about = stringResource(id = R.string.about)
            ListItem(
                leadingContent = {
                    Icon(
                        Icons.Filled.ContactPage,
                        about
                    )
                },
                headlineContent = { Text(
                    text = about,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                ) },
                modifier = Modifier.clickable {
                    aboutDialog.show()
                }
            )
        }
    }
}

@Composable
fun UninstallItem(
    navigator: DestinationsNavigator,
    withLoading: suspend (suspend () -> Unit) -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uninstallConfirmDialog = rememberConfirmDialog()
    val showTodo = {
        Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show()
    }
    val uninstallDialog = rememberUninstallDialog { uninstallType ->
        scope.launch {
            val result = uninstallConfirmDialog.awaitConfirm(
                title = context.getString(uninstallType.title),
                content = context.getString(uninstallType.message)
            )
            if (result == ConfirmResult.Confirmed) {
                withLoading {
                    when (uninstallType) {
                        UninstallType.TEMPORARY -> showTodo()
                        UninstallType.PERMANENT -> navigator.navigate(
                            FlashScreenDestination(FlashIt.FlashUninstall)
                        )
                        UninstallType.RESTORE_STOCK_IMAGE -> navigator.navigate(
                            FlashScreenDestination(FlashIt.FlashRestore)
                        )
                        UninstallType.NONE -> Unit
                    }
                }
            }
        }
    }
    val uninstall = stringResource(id = R.string.settings_uninstall)
    ListItem(
        leadingContent = {
            Icon(
                Icons.Filled.Delete,
                uninstall
            )
        },
        headlineContent = { Text(
            text = uninstall,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        ) },
        modifier = Modifier.clickable {
            uninstallDialog.show()
        }
    )
}

enum class UninstallType(val title: Int, val message: Int, val icon: ImageVector) {
    TEMPORARY(
        R.string.settings_uninstall_temporary,
        R.string.settings_uninstall_temporary_message,
        Icons.Filled.Delete
    ),
    PERMANENT(
        R.string.settings_uninstall_permanent,
        R.string.settings_uninstall_permanent_message,
        Icons.Filled.DeleteForever
    ),
    RESTORE_STOCK_IMAGE(
        R.string.settings_restore_stock_image,
        R.string.settings_restore_stock_image_message,
        Icons.AutoMirrored.Filled.Undo
    ),
    NONE(0, 0, Icons.Filled.Delete)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun rememberUninstallDialog(onSelected: (UninstallType) -> Unit): DialogHandle {
    return rememberCustomDialog { dismiss ->
        val options = listOf(
            // UninstallType.TEMPORARY,
            UninstallType.PERMANENT,
            UninstallType.RESTORE_STOCK_IMAGE
        )
        val listOptions = options.map {
            ListOption(
                titleText = stringResource(it.title),
                subtitleText = if (it.message != 0) stringResource(it.message) else null,
                icon = IconSource(it.icon)
            )
        }

        var selection = UninstallType.NONE
        ListDialog(state = rememberUseCaseState(visible = true, onFinishedRequest = {
            if (selection != UninstallType.NONE) {
                onSelected(selection)
            }
        }, onCloseRequest = {
            dismiss()
        }), header = Header.Default(
            title = stringResource(R.string.settings_uninstall),
        ), selection = ListSelection.Single(
            showRadioButtons = false,
            options = listOptions,
        ) { index, _ ->
            selection = options[index]
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    TopAppBar(
        title = { Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
        ) },
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
        scrollBehavior = scrollBehavior
    )
}

@Preview
@Composable
private fun SettingsPreview() {
    SettingScreen(EmptyDestinationsNavigator)
}