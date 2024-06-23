package com.example.projetofeevale.ui.createOccurrence.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.projetofeevale.BuildConfig;
import com.example.projetofeevale.R;
import com.example.projetofeevale.data.model.request.OccurrenceRequest;
import com.example.projetofeevale.data.model.response.OccurrenceResponse;
import com.example.projetofeevale.data.remote.api.ApiCallback;
import com.example.projetofeevale.data.remote.repository.OccurrenceRepository;
import com.example.projetofeevale.fragments.HomeFragment;
import com.example.projetofeevale.ui.createOccurrence.fragment.OccurrenceFragment;
import com.example.projetofeevale.ui.createOccurrence.fragment.OccurrenceIdentity;
import com.example.projetofeevale.ui.createOccurrence.fragment.OccurrenceImageUpload;
import com.example.projetofeevale.ui.createOccurrence.fragment.OccurrenceInfo;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class CreateOccurrence extends Fragment {
    private OccurrenceRequest occurrenceRequest;
    private FragmentContainerView fragmentContainerView;
    private int currentIndex = 0;
    private OccurrenceFragment currentFragment;
    private List<OccurrenceFragment> fragmentList = new ArrayList<>();
    private Button prevButton;
    private Button nextButton;

    private DialogInterface.OnClickListener onSuccess;

    public CreateOccurrence() {
    }

    public CreateOccurrence(DialogInterface.OnClickListener onSuccess) {
        this.onSuccess = onSuccess;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_pin, container, false);

        prevButton = view.findViewById(R.id.form_btn_prev);
        nextButton = view.findViewById(R.id.form_btn_next);

        setupPrevButton();
        setupNextButton();

        fragmentContainerView = view.findViewById(R.id.fragment_form_steps);

        occurrenceRequest = new OccurrenceRequest();

        fragmentList.add(new OccurrenceImageUpload(occurrenceRequest));
        fragmentList.add(new OccurrenceInfo(occurrenceRequest));
        fragmentList.add(new OccurrenceIdentity(occurrenceRequest));

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        replaceFormFragment(fragmentList.get(0), false, false);
    }

    private int getPreviousFragmentIndex() {
        currentIndex = currentIndex - 1;
        currentIndex = Math.max(currentIndex, 0);
        return currentIndex;
    }

    private int getNextFragmentIndex() {
        currentIndex = currentIndex + 1;
        currentIndex = Math.min(currentIndex, fragmentList.toArray().length - 1);
        return currentIndex;
    }

    private void setupPrevButton() {
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int targetIndex = getPreviousFragmentIndex();
                replaceFormFragment(fragmentList.get(targetIndex), true, false);
            }
        });
    }

    private void setupNextButton() {
        nextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!currentFragment.hasEmptyFields()) {
                    int targetIndex = getNextFragmentIndex();
                    replaceFormFragment(fragmentList.get(targetIndex), true, true);
                };
            }
        });
    }

    private void handleButtonVisibility() {
        if(currentIndex == 0) {
            prevButton.setVisibility(View.INVISIBLE);
        } else if (currentIndex == fragmentList.toArray().length - 1) {
            nextButton.setText("Finalizar");
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    postOccurrence();
                }
            });
        } else {
            setupNextButton();
            prevButton.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.VISIBLE);
            nextButton.setText("Avançar");
        }
    }

    private void postOccurrence() {
        for (OccurrenceFragment fragment : fragmentList) {
            fragment.fillForm();
        }

        new OccurrenceRepository().createOccurrence(occurrenceRequest, new ApiCallback<OccurrenceResponse>() {
            @Override
            public void onSuccess(OccurrenceResponse data) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setMessage("Ocorrência criada com sucesso!")
                        .setPositiveButton("OK", onSuccess)
                        .show();
            }

            @Override
            public void onFailure(String message, String cause, Throwable t) {
                Toast.makeText(requireActivity().getBaseContext(), message + cause, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void replaceFormFragment(OccurrenceFragment fragment, boolean animated, boolean toRight) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (animated && toRight) {
            fragmentTransaction.setCustomAnimations(
                    R.anim.slide_in_from_right,
                    R.anim.slide_out_to_left,
                    R.anim.slide_in_from_left,
                    R.anim.slide_out_to_right
            );
        }

        if (animated && !toRight) {
            fragmentTransaction.setCustomAnimations(
                    R.anim.slide_in_from_left,
                    R.anim.slide_out_to_right,
                    R.anim.slide_in_from_right,
                    R.anim.slide_out_to_left
            );
        }

        fragmentTransaction.replace(R.id.fragment_form_steps, fragment);
        currentFragment = fragment;
        handleButtonVisibility();
        fragmentTransaction.commit();
    }
}
