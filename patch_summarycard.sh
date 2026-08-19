sed -i '/fun SummaryCard(/,/^}/c\
@Composable\
fun SummaryCard(\
    title: String,\
    amount: String,\
    contentColor: Color,\
    containerColor: Color,\
    modifier: Modifier = Modifier\
) {\
    Card(\
        colors = CardDefaults.cardColors(containerColor = containerColor),\
        modifier = modifier\
            .height(90.dp),\
        shape = RoundedCornerShape(12.dp)\
    ) {\
        Column(\
            modifier = Modifier\
                .fillMaxSize()\
                .padding(12.dp),\
            verticalArrangement = Arrangement.Center,\
            horizontalAlignment = Alignment.CenterHorizontally\
        ) {\
            Text(title, color = Slate500, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)\
            Spacer(modifier = Modifier.height(4.dp))\
            Text(amount, color = contentColor, fontSize = 18.sp, fontWeight = FontWeight.Bold)\
        }\
    }\
}' app/src/main/java/com/example/ui/screens/WageLedgerScreen.kt
