package com.manrique.trailerstock.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.manrique.trailerstock.R;
import com.manrique.trailerstock.utils.FormatHelper;
import com.manrique.trailerstock.utils.PreferencesManager;

/**
 * Fragment para configurar formatos de moneda, números y fechas
 */
public class SettingsFragment extends Fragment {

    private PreferencesManager preferencesManager;

    // UI Components
    private Spinner spinnerCurrency;
    private RadioGroup radioGroupNumberFormat;
    private RadioButton radioCommaFormat, radioDotFormat;
    private RadioGroup radioGroupDateFormat;
    private RadioButton radioDmyFormat, radioMdyFormat, radioYmdFormat;
    private TextView tvCurrencyExample, tvNumberExample, tvDateExample;
    private MaterialButton btnApplySettings;

    // Valores temporales (antes de aplicar)
    private String selectedCurrency;
    private String selectedNumberFormat;
    private String selectedDateFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Inicializar PreferencesManager
        preferencesManager = PreferencesManager.getInstance(requireContext());

        // Inicializar vistas
        initViews(view);

        // Cargar configuración actual
        loadCurrentSettings();

        // Configurar listeners
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        spinnerCurrency = view.findViewById(R.id.spinner_currency);
        radioGroupNumberFormat = view.findViewById(R.id.radio_group_number_format);
        radioCommaFormat = view.findViewById(R.id.radio_comma_format);
        radioDotFormat = view.findViewById(R.id.radio_dot_format);
        radioGroupDateFormat = view.findViewById(R.id.radio_group_date_format);
        radioDmyFormat = view.findViewById(R.id.radio_dmy_format);
        radioMdyFormat = view.findViewById(R.id.radio_mdy_format);
        radioYmdFormat = view.findViewById(R.id.radio_ymd_format);
        tvCurrencyExample = view.findViewById(R.id.tv_currency_example);
        tvNumberExample = view.findViewById(R.id.tv_number_example);
        tvDateExample = view.findViewById(R.id.tv_date_example);
        btnApplySettings = view.findViewById(R.id.btn_apply_settings);

        // Configurar spinner de monedas
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                FormatHelper.getSupportedCurrencyNames());
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCurrency.setAdapter(adapter);
    }

    private void loadCurrentSettings() {
        // Cargar moneda actual
        String currentCurrency = preferencesManager.getCurrencyCode();
        selectedCurrency = currentCurrency;
        String[] currencies = FormatHelper.getSupportedCurrencies();
        for (int i = 0; i < currencies.length; i++) {
            if (currencies[i].equals(currentCurrency)) {
                spinnerCurrency.setSelection(i);
                break;
            }
        }

        // Cargar formato de números actual
        String currentNumberFormat = preferencesManager.getNumberFormat();
        selectedNumberFormat = currentNumberFormat;
        if (PreferencesManager.NUMBER_FORMAT_COMMA.equals(currentNumberFormat)) {
            radioCommaFormat.setChecked(true);
        } else {
            radioDotFormat.setChecked(true);
        }

        // Cargar formato de fecha actual
        String currentDateFormat = preferencesManager.getDateFormat();
        selectedDateFormat = currentDateFormat;
        switch (currentDateFormat) {
            case PreferencesManager.DATE_FORMAT_DMY:
                radioDmyFormat.setChecked(true);
                break;
            case PreferencesManager.DATE_FORMAT_MDY:
                radioMdyFormat.setChecked(true);
                break;
            case PreferencesManager.DATE_FORMAT_YMD:
                radioYmdFormat.setChecked(true);
                break;
        }

        // Actualizar ejemplos
        updateExamples();
    }

    private void setupListeners() {
        // Listener para cambio de moneda
        spinnerCurrency.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCurrency = FormatHelper.getSupportedCurrencies()[position];
                updateExamples();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Listener para cambio de formato de números
        radioGroupNumberFormat.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_comma_format) {
                selectedNumberFormat = PreferencesManager.NUMBER_FORMAT_COMMA;
            } else {
                selectedNumberFormat = PreferencesManager.NUMBER_FORMAT_DOT;
            }
            updateExamples();
        });

        // Listener para cambio de formato de fecha
        radioGroupDateFormat.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_dmy_format) {
                selectedDateFormat = PreferencesManager.DATE_FORMAT_DMY;
            } else if (checkedId == R.id.radio_mdy_format) {
                selectedDateFormat = PreferencesManager.DATE_FORMAT_MDY;
            } else {
                selectedDateFormat = PreferencesManager.DATE_FORMAT_YMD;
            }
            updateExamples();
        });

        // Listener para botón aplicar
        btnApplySettings.setOnClickListener(v -> applySettings());
    }

    private void updateExamples() {
        // Ejemplo de moneda
        String currencySymbol = FormatHelper.getCurrencySymbol(selectedCurrency);
        String numberExample = formatExampleNumber(1234.56, selectedNumberFormat);
        tvCurrencyExample.setText(currencySymbol + " " + numberExample);

        // Ejemplo de número
        tvNumberExample.setText(formatExampleNumber(1234567.89, selectedNumberFormat));

        // Ejemplo de fecha
        tvDateExample.setText(formatExampleDate(selectedDateFormat));
    }

    private String formatExampleNumber(double number, String format) {
        if (PreferencesManager.NUMBER_FORMAT_COMMA.equals(format)) {
            // Formato con coma decimal
            return String.format("%.2f", number)
                    .replace(".", ",")
                    .replaceAll("(\\d)(?=(\\d{3})+(?!\\d))", "$1.");
        } else {
            // Formato con punto decimal
            return String.format("%,.2f", number);
        }
    }

    private String formatExampleDate(String format) {
        switch (format) {
            case PreferencesManager.DATE_FORMAT_MDY:
                return "02/04/2026";
            case PreferencesManager.DATE_FORMAT_YMD:
                return "2026-02-04";
            case PreferencesManager.DATE_FORMAT_DMY:
            default:
                return "04/02/2026";
        }
    }

    private void applySettings() {
        // Guardar configuración
        preferencesManager.setCurrencyCode(selectedCurrency);
        preferencesManager.setNumberFormat(selectedNumberFormat);
        preferencesManager.setDateFormat(selectedDateFormat);

        // Mostrar mensaje de confirmación
        Toast.makeText(requireContext(),
                "Configuración guardada. Los cambios se aplicarán al reiniciar la app.",
                Toast.LENGTH_LONG).show();

        // Opcionalmente, volver atrás
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }
}
