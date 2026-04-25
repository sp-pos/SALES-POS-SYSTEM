package com.example.salespossystem.ui

import com.example.salespossystem.viewmodel.SalesViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salespossystem.ui.theme.*

@Composable
fun CountriesScreen(viewModel: SalesViewModel = viewModel()) {
    var searchQuery by remember { mutableStateOf("") }

    val countries = listOf(
        CountryItem("Afghanistan", "AF"),
        CountryItem("Albania", "AL"),
        CountryItem("Algeria", "DZ"),
        CountryItem("American Samoa", "AS"),
        CountryItem("Andorra", "AD"),
        CountryItem("Angola", "AO"),
        CountryItem("Anguilla", "AI"),
        CountryItem("Antarctica", "AQ"),
        CountryItem("Argentina", "AR"),
        CountryItem("Armenia", "AM"),
        CountryItem("Aruba", "AW"),
        CountryItem("Australia", "AU"),
        CountryItem("Austria", "AT"),
        CountryItem("Azerbaijan", "AZ"),
        CountryItem("Bahamas", "BS"),
        CountryItem("Bahrain", "BH"),
        CountryItem("Bangladesh", "BD"),
        CountryItem("Barbados", "BB"),
        CountryItem("Belarus", "BY"),
        CountryItem("Belgium", "BE"),
        CountryItem("Belize", "BZ"),
        CountryItem("Benin", "BJ"),
        CountryItem("Bermuda", "BM"),
        CountryItem("Bhutan", "BT"),
        CountryItem("Bolivia", "BO"),
        CountryItem("Bosnia and Herzegovina", "BA"),
        CountryItem("Botswana", "BW"),
        CountryItem("Brazil", "BR"),
        CountryItem("Brunei", "BN"),
        CountryItem("Bulgaria", "BG"),
        CountryItem("Burkina Faso", "BF"),
        CountryItem("Burundi", "BI"),
        CountryItem("Cambodia", "KH"),
        CountryItem("Cameroon", "CM"),
        CountryItem("Canada", "CA"),
        CountryItem("Cape Verde", "CV"),
        CountryItem("Cayman Islands", "KY"),
        CountryItem("Central African Republic", "CF"),
        CountryItem("Chad", "TD"),
        CountryItem("Chile", "CL"),
        CountryItem("China", "CN"),
        CountryItem("Colombia", "CO"),
        CountryItem("Comoros", "KM"),
        CountryItem("Congo", "CG"),
        CountryItem("Cook Islands", "CK"),
        CountryItem("Costa Rica", "CR"),
        CountryItem("Croatia", "HR"),
        CountryItem("Cuba", "CU"),
        CountryItem("Cyprus", "CY"),
        CountryItem("Czech Republic", "CZ"),
        CountryItem("Denmark", "DK"),
        CountryItem("Djibouti", "DJ"),
        CountryItem("Dominica", "DM"),
        CountryItem("Dominican Republic", "DO"),
        CountryItem("Ecuador", "EC"),
        CountryItem("Egypt", "EG"),
        CountryItem("El Salvador", "SV"),
        CountryItem("Equatorial Guinea", "GQ"),
        CountryItem("Eritrea", "ER"),
        CountryItem("Estonia", "EE"),
        CountryItem("Ethiopia", "ET"),
        CountryItem("Falkland Islands", "FK"),
        CountryItem("Faroe Islands", "FO"),
        CountryItem("Fiji", "FJ"),
        CountryItem("Finland", "FI"),
        CountryItem("France", "FR"),
        CountryItem("French Guiana", "GF"),
        CountryItem("French Polynesia", "PF"),
        CountryItem("Gabon", "GA"),
        CountryItem("Gambia", "GM"),
        CountryItem("Georgia", "GE"),
        CountryItem("Germany", "DE"),
        CountryItem("Ghana", "GH"),
        CountryItem("Gibraltar", "GI"),
        CountryItem("Greece", "GR"),
        CountryItem("Greenland", "GL"),
        CountryItem("Grenada", "GD"),
        CountryItem("Guadeloupe", "GP"),
        CountryItem("Guam", "GU"),
        CountryItem("Guatemala", "GT"),
        CountryItem("Guinea", "GN"),
        CountryItem("Guinea-Bissau", "GW"),
        CountryItem("Guyana", "GY"),
        CountryItem("Haiti", "HT"),
        CountryItem("Honduras", "HN"),
        CountryItem("Hong Kong", "HK"),
        CountryItem("Hungary", "HU"),
        CountryItem("Iceland", "IS"),
        CountryItem("India", "IN"),
        CountryItem("Indonesia", "ID"),
        CountryItem("Iran", "IR"),
        CountryItem("Iraq", "IQ"),
        CountryItem("Ireland", "IE"),
        CountryItem("Israel", "IL"),
        CountryItem("Italy", "IT"),
        CountryItem("Jamaica", "JM"),
        CountryItem("Japan", "JP"),
        CountryItem("Jordan", "JO"),
        CountryItem("Kazakhstan", "KZ"),
        CountryItem("Kenya", "KE"),
        CountryItem("Kiribati", "KI"),
        CountryItem("Kuwait", "KW"),
        CountryItem("Kyrgyzstan", "KG"),
        CountryItem("Laos", "LA"),
        CountryItem("Latvia", "LV"),
        CountryItem("Lebanon", "LB"),
        CountryItem("Lesotho", "LS"),
        CountryItem("Liberia", "LR"),
        CountryItem("Libya", "LY"),
        CountryItem("Liechtenstein", "LI"),
        CountryItem("Lithuania", "LT"),
        CountryItem("Luxembourg", "LU"),
        CountryItem("Macao", "MO"),
        CountryItem("Macedonia", "MK"),
        CountryItem("Madagascar", "MG"),
        CountryItem("Malawi", "MW"),
        CountryItem("Malaysia", "MY"),
        CountryItem("Maldives", "MV"),
        CountryItem("Mali", "ML"),
        CountryItem("Malta", "MT"),
        CountryItem("Marshall Islands", "MH"),
        CountryItem("Martinique", "MQ"),
        CountryItem("Mauritania", "MR"),
        CountryItem("Mauritius", "MU"),
        CountryItem("Mayotte", "YT"),
        CountryItem("Mexico", "MX"),
        CountryItem("Micronesia", "FM"),
        CountryItem("Moldova", "MD"),
        CountryItem("Monaco", "MC"),
        CountryItem("Mongolia", "MN"),
        CountryItem("Montserrat", "MS"),
        CountryItem("Morocco", "MA"),
        CountryItem("Mozambique", "MZ"),
        CountryItem("Myanmar", "MM"),
        CountryItem("Namibia", "NA"),
        CountryItem("Nauru", "NR"),
        CountryItem("Nepal", "NP"),
        CountryItem("Netherlands", "NL"),
        CountryItem("New Caledonia", "NC"),
        CountryItem("New Zealand", "NZ"),
        CountryItem("Nicaragua", "NI"),
        CountryItem("Niger", "NE"),
        CountryItem("Nigeria", "NG"),
        CountryItem("Niue", "NU"),
        CountryItem("Norfolk Island", "NF"),
        CountryItem("North Korea", "KP"),
        CountryItem("Northern Mariana Islands", "MP"),
        CountryItem("Norway", "NO"),
        CountryItem("Oman", "OM"),
        CountryItem("Pakistan", "PK"),
        CountryItem("Palau", "PW"),
        CountryItem("Panama", "PA"),
        CountryItem("Papua New Guinea", "PG"),
        CountryItem("Paraguay", "PY"),
        CountryItem("Peru", "PE"),
        CountryItem("Philippines", "PH"),
        CountryItem("Poland", "PL"),
        CountryItem("Portugal", "PT"),
        CountryItem("Puerto Rico", "PR"),
        CountryItem("Qatar", "QA"),
        CountryItem("Reunion", "RE"),
        CountryItem("Romania", "RO"),
        CountryItem("Russia", "RU"),
        CountryItem("Rwanda", "RW"),
        CountryItem("Saint Kitts and Nevis", "KN"),
        CountryItem("Saint Lucia", "LC"),
        CountryItem("Saint Pierre and Miquelon", "PM"),
        CountryItem("Saint Vincent and the Grenadines", "VC"),
        CountryItem("Samoa", "WS"),
        CountryItem("San Marino", "SM"),
        CountryItem("Sao Tome and Principe", "ST"),
        CountryItem("Saudi Arabia", "SA"),
        CountryItem("Senegal", "SN"),
        CountryItem("Seychelles", "SC"),
        CountryItem("Sierra Leone", "SL"),
        CountryItem("Singapore", "SG"),
        CountryItem("Slovakia", "SK"),
        CountryItem("Slovenia", "SI"),
        CountryItem("Solomon Islands", "SB"),
        CountryItem("Somalia", "SO"),
        CountryItem("South Africa", "ZA"),
        CountryItem("South Korea", "KR"),
        CountryItem("Spain", "ES"),
        CountryItem("Sri Lanka", "LK"),
        CountryItem("Sudan", "SD"),
        CountryItem("Suriname", "SR"),
        CountryItem("Svalbard and Jan Mayen", "SJ"),
        CountryItem("Swaziland", "SZ"),
        CountryItem("Sweden", "SE"),
        CountryItem("Switzerland", "CH"),
        CountryItem("Syria", "SY"),
        CountryItem("Taiwan", "TW"),
        CountryItem("Tajikistan", "TJ"),
        CountryItem("Tanzania", "TZ"),
        CountryItem("Thailand", "TH"),
        CountryItem("Togo", "TG"),
        CountryItem("Tokelau", "TK"),
        CountryItem("Tonga", "TO"),
        CountryItem("Trinidad and Tobago", "TT"),
        CountryItem("Tunisia", "TN"),
        CountryItem("Turkey", "TR"),
        CountryItem("Turkmenistan", "TM"),
        CountryItem("Turks and Caicos Islands", "TC"),
        CountryItem("Tuvalu", "TV"),
        CountryItem("Uganda", "UG"),
        CountryItem("Ukraine", "UA"),
        CountryItem("United Arab Emirates", "AE"),
        CountryItem("United Kingdom", "GB"),
        CountryItem("United States", "US"),
        CountryItem("Uruguay", "UY"),
        CountryItem("Uzbekistan", "UZ"),
        CountryItem("Vanuatu", "VU"),
        CountryItem("Vatican City", "VA"),
        CountryItem("Venezuela", "VE"),
        CountryItem("Vietnam", "VN"),
        CountryItem("Virgin Islands, British", "VG"),
        CountryItem("Virgin Islands, U.S.", "VI"),
        CountryItem("Wallis and Futuna", "WF"),
        CountryItem("Western Sahara", "EH"),
        CountryItem("Yemen", "YE"),
        CountryItem("Zambia", "ZM"),
        CountryItem("Zimbabwe", "ZW")
    )

    // বর্তমান সিলেক্ট করা দেশটিকে তালিকার সবার উপরে রাখার লজিক
    val filteredCountries = countries.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery, ignoreCase = true)
    }.sortedByDescending { it.code == viewModel.selectedCountryCode }

    Column(modifier = Modifier.fillMaxSize().background(DashboardBackground)) {
        CountriesTopBar(onRefresh = { searchQuery = "" })
        
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            placeholder = { Text("Search country...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SidebarSelected,
                unfocusedBorderColor = Color.DarkGray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f).background(Color.Black)) {
            items(filteredCountries, key = { it.code }) { item ->
                val isSelected = viewModel.selectedCountryCode == item.code
                CountryListItem(
                    item = item, 
                    isSelected = isSelected,
                    onClick = { 
                        viewModel.updateCountry(item.code)
                    }
                )
                HorizontalDivider(color = Color.DarkGray, thickness = 0.5.dp)
            }
        }
    }
}

@Composable
fun CountriesTopBar(onRefresh: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(HeaderBackground)
            .padding(horizontal = 8.dp)
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ProductAction(Icons.Default.Refresh, "Refresh", onClick = onRefresh)
        ProductAction(Icons.Default.Add, "New country")
        ProductAction(Icons.Default.Edit, "Edit")
        ProductAction(Icons.Default.Delete, "Delete")
        Spacer(modifier = Modifier.width(8.dp))
        VerticalDivider(modifier = Modifier.height(30.dp), color = Color.DarkGray)
        ProductAction(Icons.AutoMirrored.Filled.Help, "Help")
    }
}

@Composable
fun CountryListItem(item: CountryItem, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) SidebarSelected.copy(alpha = 0.3f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        // Country Code Circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(if (isSelected) SidebarSelected else SidebarSelected.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                .border(1.dp, SidebarSelected, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(item.code, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = item.name,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        
        if (isSelected) {
            Icon(Icons.Default.Check, contentDescription = "Selected", tint = SidebarSelected)
        } else {
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.DarkGray)
        }
    }
}

data class CountryItem(val name: String, val code: String)

@Preview(widthDp = 360, heightDp = 640)
@Composable
fun CountriesScreenPreview() {
    CountriesScreen()
}
