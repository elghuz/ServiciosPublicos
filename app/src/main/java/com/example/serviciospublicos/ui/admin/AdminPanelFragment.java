package com.example.serviciospublicos.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.serviciospublicos.R;

public class AdminPanelFragment extends Fragment {

    public AdminPanelFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_panel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnGestionObras = view.findViewById(R.id.btnGestionObras);
        btnGestionObras.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_adminPanelFragment_to_adminObrasFragment)
        );
    }
}
