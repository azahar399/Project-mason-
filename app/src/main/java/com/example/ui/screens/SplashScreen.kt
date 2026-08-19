package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onNavigateToDashboard: () -> Unit) {
    // Animation states
    val scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Run entry bounce animation for the main logo
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    LaunchedEffect(Unit) {
        // Display splash for 2.2 seconds and then transition
        delay(2200)
        onNavigateToDashboard()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Split screen background mimicking the design precisely
        Column(modifier = Modifier.fillMaxSize()) {
            // Top half: Warm Cream background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.1f)
                    .background(Color(0xFFFAF7F0))
            )
            // Bottom half: Elegant Dark Slate background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
                    .background(Color(0xFF1E2530))
            )
        }

        // Content overlay
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- TOP HEADER CONTENT ---
            Spacer(modifier = Modifier.height(72.dp))
            
            Text(
                text = "PROJECT",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Text(
                text = "MASON",
                fontSize = 46.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0F172A),
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Smart Construction Record Management",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569),
                letterSpacing = 0.5.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            // --- CENTER LOGO OVERLAY (Positioned perfectly on the background split line) ---
            Spacer(modifier = Modifier.weight(1f))
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(220.dp)
                    .scale(scale.value)
            ) {
                // Radial golden gradient underlay for a soft glow
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0x2BD97706),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Beautiful custom vector shield icon using standard Material icons for safety
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Project Mason Emblem",
                    modifier = Modifier.size(100.dp),
                    tint = Color(0xFFFDE047)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- BOTTOM TYPOGRAPHY CONTENT ---
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 72.dp)
            ) {
                Text(
                    text = "PROJECT TRACKING",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFF1F5F9),
                    letterSpacing = 3.sp,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "&",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFDE047),
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "REPORTING",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFF1F5F9),
                    letterSpacing = 3.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
