package com.pte.clientapptestintegration

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.pte.clientapptestintegration.R
import com.pte.clientapptestintegration.fragments.FragmentExample1
import com.pte.clientapptestintegration.fragments.FragmentExample2
import com.pte.clientapptestintegration.fragments.FragmentExample3

class MainActivity : AppCompatActivity() {

    private lateinit var drawer: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Initialize your DrawerLayout and ActionBarDrawerToggle
        drawer = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(this, drawer, R.string.drawer_open, R.string.drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        // Enable the Up button for navigation
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Set up the navigation view
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            // Handle navigation view item clicks here
            when (menuItem.itemId) {
                R.id.fragmentExample1 -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentExample1()).commit()
                }
                R.id.fragmentExample2 -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentExample2()).commit()
                }
                R.id.fragmentExample3 -> {
                    supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentExample3()).commit()
                }
            }
            // Close the drawer after selecting an item
            drawer.closeDrawer(GravityCompat.START)
            true
        }

        // Set the initial fragment
        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentExample1()).commit()
    }

    // Handle navigation drawer toggle events
    /*override fun onSupportNavigateUp(): Boolean {
        return if (toggle.onOptionsItemSelected()) {
            true
        } else super.onSupportNavigateUp()
    }*/

    // Handle back button press to close the navigation drawer if it's open
    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
