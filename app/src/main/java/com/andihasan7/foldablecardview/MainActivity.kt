
package com.andihasan7.foldablecardview

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.andihasan7.foldablecardview.databinding.ActivityMainBinding
import com.andihasan7.foldablecardview.databinding.LocationBinding
import com.andihasan7.foldablecardview.databinding.PassengersBinding
import com.andihasan7.foldablecardview.databinding.DatesBinding

public class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    
    private val binding: ActivityMainBinding
      get() = checkNotNull(_binding) { "Activity has been destroyed" }
      
    private lateinit var uri: Uri
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate and get instance of binding
        _binding = ActivityMainBinding.inflate(layoutInflater)

        // set content view to binding's root
        setContentView(binding.root)
        
        // Toolbar setup
        setSupportActionBar(binding.mainToolbar)
        binding.mainCollapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE)
        binding.mainCollapsingToolbarLayout.setExpandedTitleTextColor(ColorStateList.valueOf(Color.WHITE))

        // Setup inner view from ExpandableCardView: location
        binding.mainLocation.setOnExpandedListener { view, isExpanded ->
            val innerView = view?.findViewById<android.view.ViewStub>(com.andihasan7.foldablecardview.R.id.card_stub)?.inflatedId
                ?.let { view.findViewById<android.view.View>(it) }

            if (innerView != null) {
                val locationBinding = LocationBinding.bind(innerView)
                locationBinding.buttonMaps.setOnClickListener {
                    uri = Uri.parse("geo:25.7906500,-80.1300500?q=Miami Beach&z=10")
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            }
        }

        // Setup profile card listener
        binding.mainProfileCard.setOnExpandedListener { _, isExpanded ->
            val message = if (isExpanded) "Expanded!" else "Collapsed!"
            Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
        }

        // You can setup passengersBinding or datesBinding similarly if needed
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.aboutme -> {
                uri = Uri.parse("https://www.alessandrosperotti.com")
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
            R.id.getlibrary -> {
                uri = Uri.parse("https://github.com/AleSpero/ExpandableCardView")
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
            R.id.myotherapps -> {
                uri = Uri.parse("market://search?q=alessandro%20sperotti")
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            }
        }
        return super.onOptionsItemSelected(item)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
