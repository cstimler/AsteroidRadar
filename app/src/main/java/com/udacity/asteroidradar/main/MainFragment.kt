package com.udacity.asteroidradar.main

import android.media.Image
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatDrawableManager.get
import androidx.appcompat.widget.ResourceManagerInternal.get
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ProcessLifecycleOwner.get
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.work.Logger.get
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding
import com.udacity.asteroidradar.repository.AsteroidsRepository
import kotlinx.android.synthetic.main.fragment_detail.*
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.fragment_main.activity_main_image_of_the_day
import okhttp3.internal.platform.Platform.get
import okhttp3.internal.publicsuffix.PublicSuffixDatabase.get

@RequiresApi(Build.VERSION_CODES.O)
class MainFragment : Fragment() {


    private val viewModel: MainViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onViewCreated"
            ViewModelProvider(this).get(MainViewModel::class.java)
        }
        ViewModelProvider(
            this,
            MainViewModel.Factory(activity.application)
        ).get(MainViewModel::class.java)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = this

        binding.viewModel = viewModel
        binding.asteroidRecycler.adapter = AsteroidAdapter(AsteroidAdapter.OnClickListener {
            viewModel.displayAsteroidDetails(it)
        })

        setHasOptionsMenu(true)

        viewModel.navigateToSelectedAsteroid.observe(viewLifecycleOwner, Observer {
            if (null != it) {
                this.findNavController().navigate(MainFragmentDirections.actionShowDetail(it))
                viewModel.displayAsteroidDetailsComplete()
            }
        })

            viewModel.readyToDownloadPicasso.observe(viewLifecycleOwner, Observer {
                if (it) {
                    getPhotoFromPicasso()
                    viewModel.notTimeToDownloadPicasso()
                }
            })
        // Need to complete navigation

        return binding.root
    }

    override fun onStart() {
        Log.i("CHARLES: about to hit refreshPhotoOfTheDay", "")
        viewModel.refreshPhotoOfTheDay()
        super.onStart()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getPhotoFromPicasso() {
        val imageView: ImageView? = view?.findViewById(R.id.activity_main_image_of_the_day)
        /*
        val picasso = Picasso.Builder(context)
            .listener {_, _, e -> e.printStackTrace()}
            .build()
            */

        Picasso.with(context).load(viewModel.picUrl2).into(imageView)
        // Good place to add content description of the ImageView for TalkBack
        imageView?.contentDescription = viewModel.imageTitle
    }



    /* Not sure I need this:
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            viewModel.asteroidRetrievedList.observe(viewLifecycleOwner, Observer<List<Asteroid>> {

            })
        }
    */
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_overflow_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.chooseList(
            when (item.itemId) {
                R.id.show_week_asteroids -> 1
                R.id.show_today_asteroids -> 2
                R.id.show_saved_asteroids -> 3
                else -> 1
            }
        )
       // onStart()
        return true
    }
}
