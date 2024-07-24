package com.pro.music.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.pro.music.MyApplication
import com.pro.music.R
import com.pro.music.activity.MainActivity
import com.pro.music.activity.PlayMusicActivity
import com.pro.music.adapter.ArtistHorizontalAdapter
import com.pro.music.adapter.BannerSongAdapter
import com.pro.music.adapter.CategoryAdapter
import com.pro.music.adapter.SongAdapter
import com.pro.music.adapter.SongPopularAdapter
import com.pro.music.constant.Constant
import com.pro.music.constant.GlobalFunction
import com.pro.music.databinding.FragmentHomeBinding
import com.pro.music.listener.IOnClickArtistItemListener
import com.pro.music.listener.IOnClickCategoryItemListener
import com.pro.music.listener.IOnClickSongItemListener
import com.pro.music.model.Artist
import com.pro.music.model.Category
import com.pro.music.model.Song
import com.pro.music.service.MusicService
import com.pro.music.service.MusicService.Companion.clearListSongPlaying
import java.util.Collections

class HomeFragment : Fragment() {

    private var binding: FragmentHomeBinding? = null
    private var mListCategory: MutableList<Category>? = null
    private var mListArtist: MutableList<Artist>? = null
    private var mListSong: MutableList<Song>? = null
    private var mListSongBanner: MutableList<Song>? = null
    private var mSongPopularAdapter: SongPopularAdapter? = null
    private val mHandlerBanner = Handler(Looper.getMainLooper())
    private val mRunnableBanner = Runnable {
        if (mListSongBanner == null || mListSongBanner!!.isEmpty()) {
            return@Runnable
        }
        if (binding?.viewpager2?.currentItem == mListSongBanner!!.size - 1) {
            binding?.viewpager2?.currentItem = 0
            return@Runnable
        }
        binding?.viewpager2?.currentItem = binding?.viewpager2!!.currentItem + 1
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        loadListCategoryFromFirebase()
        loadListArtistFromFirebase()
        loadListSongFromFirebase()
        initListener()
        return binding?.root
    }

    private fun initListener() {
        binding?.layoutSearch?.setOnClickListener { searchSong() }
        binding?.layoutViewAllCategory?.setOnClickListener {
            val mainActivity = activity as MainActivity?
            mainActivity?.clickSeeAllCategory()
        }
        binding?.layoutViewAllArtist?.setOnClickListener {
            val mainActivity = activity as MainActivity?
            mainActivity?.clickSeeAllArtist()
        }
        binding?.layoutViewAllPopular?.setOnClickListener {
            val mainActivity = activity as MainActivity?
            mainActivity?.clickSeeAllPopularSongs()
        }
        binding?.layoutViewAllFavoriteSongs?.setOnClickListener {
            val mainActivity = activity as MainActivity?
            mainActivity?.clickSeeAllFavoriteSongs()
        }
    }

    private fun loadListCategoryFromFirebase() {
        if (activity == null) return
        MyApplication[activity!!].categoryDatabaseReference()
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        mListCategory = ArrayList()
                        for (dataSnapshot in snapshot.children) {
                            val category = dataSnapshot.getValue(Category::class.java) ?: return
                            mListCategory?.add(0, category)
                        }
                        displayListCategory()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        GlobalFunction.showToastMessage(activity, getString(R.string.msg_get_date_error))
                    }
                })
    }

    private fun loadListArtistFromFirebase() {
        if (activity == null) return
        MyApplication[activity!!].artistDatabaseReference()
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        mListArtist = ArrayList()
                        for (dataSnapshot in snapshot.children) {
                            val artist = dataSnapshot.getValue(Artist::class.java) ?: return
                            mListArtist?.add(0, artist)
                        }
                        displayListArtist()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        GlobalFunction.showToastMessage(activity, getString(R.string.msg_get_date_error))
                    }
                })
    }

    private fun loadListSongFromFirebase() {
        if (activity == null) return
        MyApplication[activity!!].songsDatabaseReference()
                ?.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        mListSong = ArrayList()
                        for (dataSnapshot in snapshot.children) {
                            val song = dataSnapshot.getValue(Song::class.java) ?: return
                            mListSong?.add(0, song)
                        }
                        displayListBannerSongs()
                        displayListPopularSongs()
                        displayListFavoriteSongs()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        GlobalFunction.showToastMessage(activity, getString(R.string.msg_get_date_error))
                    }
                })
    }

    private fun displayListCategory() {
        val gridLayoutManager = GridLayoutManager(activity, 2)
        binding?.rcvCategory?.layoutManager = gridLayoutManager
        val categoryAdapter = CategoryAdapter(loadListCategory(), object : IOnClickCategoryItemListener {
            override fun onClickItemCategory(category: Category) {
                val mainActivity = activity as MainActivity?
                mainActivity?.clickOpenSongsByCategory(category)
            }
        })
        binding?.rcvCategory?.adapter = categoryAdapter
    }

    private fun displayListArtist() {
        val linearLayoutManager = LinearLayoutManager(activity,
                LinearLayoutManager.HORIZONTAL, false)
        binding?.rcvArtist?.layoutManager = linearLayoutManager
        val artistHorizontalAdapter = ArtistHorizontalAdapter(loadListArtist(), object : IOnClickArtistItemListener {
            override fun onClickItemArtist(artist: Artist) {
                val mainActivity = activity as MainActivity?
                mainActivity?.clickOpenSongsByArtist(artist)
            }
        })
        binding?.rcvArtist?.adapter = artistHorizontalAdapter
    }

    private fun displayListBannerSongs() {
        val bannerSongAdapter = BannerSongAdapter(loadListBannerSongs(), object : IOnClickSongItemListener {
            override fun onClickItemSong(song: Song) {
                goToSongDetail(song)
            }

            override fun onClickFavoriteSong(song: Song, favorite: Boolean) {}
            override fun onClickMoreOptions(song: Song) {}
        })
        binding?.viewpager2?.adapter = bannerSongAdapter
        binding?.indicator3?.setViewPager(binding?.viewpager2)
        binding?.viewpager2?.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                mHandlerBanner.removeCallbacks(mRunnableBanner)
                mHandlerBanner.postDelayed(mRunnableBanner, 3000)
            }
        })
    }

    private fun loadListBannerSongs(): List<Song> {
        if (mListSongBanner != null) {
            mListSongBanner!!.clear()
        } else {
            mListSongBanner = ArrayList()
        }
        if (mListSong == null || mListSong!!.isEmpty()) {
            return mListSongBanner!!
        }
        for (song in mListSong!!) {
            if (song.isFeatured == true && mListSongBanner!!.size < Constant.MAX_COUNT_BANNER) {
                mListSongBanner!!.add(song)
            }
        }
        return mListSongBanner!!
    }

    private fun displayListPopularSongs() {
        val linearLayoutManager = LinearLayoutManager(activity)
        binding?.rcvPopularSongs?.layoutManager = linearLayoutManager
        mSongPopularAdapter = SongPopularAdapter(activity, loadListPopularSongs(),
                object : IOnClickSongItemListener {
                    override fun onClickItemSong(song: Song) {
                        goToSongDetail(song)
                    }

                    override fun onClickMoreOptions(song: Song) {
                        GlobalFunction.handleClickMoreOptions(activity, song)
                    }

                    override fun onClickFavoriteSong(song: Song, favorite: Boolean) {
                        GlobalFunction.onClickFavoriteSong(activity, song, favorite)
                    }
                })
        binding?.rcvPopularSongs?.adapter = mSongPopularAdapter
    }

    private fun loadListPopularSongs(): List<Song> {
        val list: MutableList<Song> = ArrayList()
        if (mListSong == null || mListSong!!.isEmpty()) {
            return list
        }
        val allSongs: List<Song> = ArrayList(mListSong!!)
        Collections.sort(allSongs) { song1: Song, song2: Song -> song2.count - song1.count }
        for (song in allSongs) {
            if (list.size < Constant.MAX_COUNT_POPULAR) {
                list.add(song)
            }
        }
        return list
    }

    private fun displayListFavoriteSongs() {
        if (activity == null) return
        val list = loadListFavoriteSongs()
        if (list.isEmpty()) {
            binding?.layoutFavorite?.visibility = View.GONE
        } else {
            binding?.layoutFavorite?.visibility = View.VISIBLE
            val linearLayoutManager = LinearLayoutManager(activity)
            binding?.rcvFavoriteSongs?.layoutManager = linearLayoutManager
            val songAdapter = SongAdapter(loadListFavoriteSongs(), object : IOnClickSongItemListener {
                override fun onClickItemSong(song: Song) {
                    goToSongDetail(song)
                }

                override fun onClickFavoriteSong(song: Song, favorite: Boolean) {
                    GlobalFunction.onClickFavoriteSong(activity, song, favorite)
                }

                override fun onClickMoreOptions(song: Song) {
                    GlobalFunction.handleClickMoreOptions(activity, song)
                }
            })
            binding?.rcvFavoriteSongs?.adapter = songAdapter
        }
    }

    private fun loadListCategory(): List<Category> {
        val list: MutableList<Category> = ArrayList()
        if (mListCategory == null || mListCategory!!.isEmpty()) return list
        for (category in mListCategory!!) {
            if (list.size < Constant.MAX_COUNT_CATEGORY) {
                list.add(category)
            }
        }
        return list
    }

    private fun loadListArtist(): List<Artist> {
        val list: MutableList<Artist> = ArrayList()
        if (mListArtist == null || mListArtist!!.isEmpty()) return list
        for (artist in mListArtist!!) {
            if (list.size < Constant.MAX_COUNT_ARTIST) {
                list.add(artist)
            }
        }
        return list
    }

    private fun loadListFavoriteSongs(): List<Song> {
        val list: MutableList<Song> = ArrayList()
        if (mListSong == null || mListSong!!.isEmpty()) {
            return list
        }
        for (song in mListSong!!) {
            if (GlobalFunction.isFavoriteSong(song) && list.size < Constant.MAX_COUNT_FAVORITE) {
                list.add(song)
            }
        }
        return list
    }

    private fun searchSong() {
        val mainActivity = activity as MainActivity?
        mainActivity?.clickSearchSongScreen()
    }

    private fun goToSongDetail(song: Song) {
        clearListSongPlaying()
        MusicService.mListSongPlaying!!.add(song)
        MusicService.isPlaying = false
        GlobalFunction.startMusicService(activity, Constant.PLAY, 0)
        GlobalFunction.startActivity(activity, PlayMusicActivity::class.java)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mSongPopularAdapter != null) {
            mSongPopularAdapter!!.release()
        }
    }
}