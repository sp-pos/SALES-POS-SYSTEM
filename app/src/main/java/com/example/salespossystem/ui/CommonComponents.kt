package com.example.salespossystem.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.salespossystem.ui.theme.SALESPOSSYSTEMTheme
import com.example.salespossystem.ui.theme.SidebarSelected

@Composable
fun ProductAction(
    icon: ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Text(label, color = tint, fontSize = 10.sp)
    }
}

@Composable
fun TabItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = if (isSelected) SidebarSelected else Color.Transparent,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun FilterAction(
    icon: ImageVector,
    label: String,
    tint: Color = Color.White,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Text(label, color = tint, fontSize = 10.sp)
    }
}

@Composable
fun Checkmark(checked: Boolean, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        if (checked) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
    }
}

@Composable
fun SectionHeader(title: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 4.dp),
            color = Color.DarkGray
        )
    }
}

@Composable
fun SidebarActionSmall(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    bgColor: Color = Color.Transparent,
    onClick: () -> Unit = {}
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(50.dp)
            .padding(2.dp),
        color = bgColor,
        shape = RoundedCornerShape(4.dp),
        border = if (bgColor == Color.Transparent) BorderStroke(1.dp, Color.DarkGray) else null
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(18.dp))
            Text(label, color = Color.White, fontSize = 9.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun PaymentButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color? = null
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(50.dp)
            .padding(2.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(4.dp),
        border = BorderStroke(1.dp, accentColor ?: Color.DarkGray)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = accentColor ?: Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF333333)
@Composable
fun ProductActionPreview() {
    SALESPOSSYSTEMTheme {
        ProductAction(
            icon = Icons.Default.Save,
            label = "Save"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF333333)
@Composable
fun FilterActionPreview() {
    SALESPOSSYSTEMTheme {
        FilterAction(
            icon = Icons.Default.Add,
            label = "Add"
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun CheckmarkPreview() {
    SALESPOSSYSTEMTheme {
        Checkmark(
            checked = true,
            modifier = Modifier
                .size(24.dp)
                .background(Color.DarkGray)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF202020)
@Composable
fun SectionHeaderPreview() {
    SALESPOSSYSTEMTheme {
        SectionHeader(title = "Section Title")
    }
}
