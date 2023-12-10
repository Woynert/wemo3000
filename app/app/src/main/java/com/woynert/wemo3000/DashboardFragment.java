package com.woynert.wemo3000;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.woynert.wemo3000.databinding.FragmentDashboardBinding;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private Controller controller;
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
                if (controller.peer == null) return;
                new Thread(() -> {
                    String toastMsg = "✅️️️ Señal de apagado enviada.";
                    if (!RestClient.shutdown(controller.peer.ip, controller.peer.port, 5000)) {
                        toastMsg = "❌️ Hubo un error al enviar la señal.";
                    }
                    Snackbar.make(view, toastMsg, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                }).start();
            }
        });

        // init app logic
        controller = new Controller();
        controller.setup(view);

        // view update loop
        final Handler handler = new Handler();
        final int delay = 1500;

        handler.postDelayed(new Runnable() {
            public void run() {
                updateViewLoop();
                handler.postDelayed(this, delay);
            }
        }, delay);

        updateView();
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

        if (controller.peer == null) return;
        else if (binding.viewPeerCard.getVisibility() == View.GONE) {
            binding.viewPeerCard.setVisibility(View.VISIBLE);
        }

        binding.textViewPeerHostname.setText(controller.peer.hostname);
        binding.textViewPeerAddress.setText(controller.peer.ip + ":" + controller.peer.port);
        long seconds = (System.currentTimeMillis() - controller.peer.lastTimeActive.getTime()) / 1000;
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
        controller.stop();
        binding = null;
    }
}