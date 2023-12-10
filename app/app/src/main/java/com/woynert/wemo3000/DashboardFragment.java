package com.woynert.wemo3000;

import static androidx.appcompat.widget.TintTypedArray.obtainStyledAttributes;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.woynert.wemo3000.databinding.FragmentDashboardBinding;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private Head logic;
    private int visualLoadingCycle = 0;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        binding.viewPeerCard.setVisibility(View.GONE);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(DashboardFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
                Log.d("TAG", "click");
            }
        });

        // init app logic
        logic = new Head();
        logic.setup(view);

        // view update loop
        final Handler handler = new Handler();
        final int delay = 1500;

        handler.postDelayed(new Runnable() {
            public void run() {
                updateViewLoop();
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    private void updateViewLoop () {
        updateView();
    }

    private void updateView () {
        if (binding == null) return;

        // "..." dots loading animation cycle
        StringBuilder dotsText = new StringBuilder("");
        for (int i = 0; i < visualLoadingCycle; i++) {
            dotsText.append(".");
        }
        visualLoadingCycle++;
        visualLoadingCycle = visualLoadingCycle % 4;
        binding.textViewSearchingDevices.setText(getString(R.string.searching_devices_label, dotsText.toString()));

        if (logic.peer == null) return;
        else if (binding.viewPeerCard.getVisibility() == View.GONE) {
            binding.viewPeerCard.setVisibility(View.VISIBLE);
        }

        binding.textViewPeerHostname.setText(logic.peer.hostname);
        binding.textViewPeerAddress.setText(logic.peer.ip + ":" + logic.peer.port);
        long seconds = (System.currentTimeMillis() - logic.peer.lastTimeActive.getTime()) / 1000;
        binding.textViewPeerLastTime.setText(String.format("Activo hace %d segundos", seconds));

        if (seconds < 12) {
            binding.textViewPeerLastTime.setTextColor(ContextCompat.getColor(this.getContext(), com.google.android.material.R.color.design_default_color_secondary));
        }
        else{
            binding.textViewPeerLastTime.setTextColor(ContextCompat.getColor(this.getContext(), com.google.android.material.R.color.design_error));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}