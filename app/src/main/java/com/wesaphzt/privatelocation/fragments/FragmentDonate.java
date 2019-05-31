package com.wesaphzt.privatelocation.fragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.wesaphzt.privatelocation.R;

import es.dmoral.toasty.Toasty;

public class FragmentDonate extends Fragment {

    private Context context;

    private static final String BITCOIN_PREFIX = "bitcoin:";
    private static final String LITECOIN_PREFIX = "litecoin:";
    private static final String ETHEREUM_PREFIX = "ethereum:";
    private static final String MONERO_PREFIX = "monero:";

    private static String BITCOIN_ADDRESS;
    private static String BITCOIN_FULL;

    private static String LITECOIN_ADDRESS;
    private static String LITECOIN_FULL;

    private static String ETHEREUM_ADDRESS;
    private static String ETHEREUM_FULL;

    private static String MONERO_ADDRESS;
    private static String MONERO_FULL;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_donate, container, false);

        setHasOptionsMenu(true);

        context = getContext();

        final TextView tvAddressBtc = view.findViewById(R.id.donate_bitcoin_address);
        final TextView tvAddressLtc = view.findViewById(R.id.donate_litecoin_address);
        final TextView tvAddressEth = view.findViewById(R.id.donate_ethereum_address);
        final TextView tvAddressXmr = view.findViewById(R.id.donate_monero_address);

        BITCOIN_ADDRESS = getString(R.string.donate_bitcoin_address);
        BITCOIN_FULL = BITCOIN_PREFIX + BITCOIN_ADDRESS;
        tvAddressBtc.setText(BITCOIN_ADDRESS);

        LITECOIN_ADDRESS = getString(R.string.donate_litecoin_address);
        LITECOIN_FULL = LITECOIN_PREFIX + LITECOIN_ADDRESS;
        tvAddressLtc.setText(LITECOIN_ADDRESS);

        ETHEREUM_ADDRESS = getString(R.string.donate_ethereum_address);
        ETHEREUM_FULL = ETHEREUM_PREFIX + ETHEREUM_ADDRESS;
        tvAddressEth.setText(ETHEREUM_ADDRESS);

        MONERO_ADDRESS = getString(R.string.donate_monero_address);
        MONERO_FULL = MONERO_PREFIX + MONERO_ADDRESS;
        tvAddressXmr.setText(MONERO_ADDRESS);

        tvAddressBtc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //attempt to open bitcoin app, else copy to clipboard
                try {
                    openURI(BITCOIN_FULL);
                } catch(Exception ignored) {
                    copyToClipboard(BITCOIN_ADDRESS);
                }
            }
        });
        tvAddressBtc.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                copyToClipboard(BITCOIN_ADDRESS);
                return true;
            }
        });

        //litecoin
        tvAddressLtc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //attempt to open litecoin app, else copy to clipboard
                try {
                    openURI(LITECOIN_FULL);
                } catch(Exception ignored) {
                    copyToClipboard(LITECOIN_ADDRESS);
                }
            }
        });
        tvAddressLtc.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                copyToClipboard(LITECOIN_ADDRESS);
                return true;
            }
        });

        //ethereum
        tvAddressEth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //attempt to open ethereum app, else copy to clipboard
                try {
                    openURI(ETHEREUM_FULL);
                } catch(Exception ignored) {
                    copyToClipboard(ETHEREUM_ADDRESS);
                }
            }
        });
        tvAddressEth.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                copyToClipboard(ETHEREUM_ADDRESS);
                return true;
            }
        });

        //monero
        tvAddressXmr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //attempt to open ethereum app, else copy to clipboard
                try {
                    openURI(MONERO_FULL);
                } catch(Exception ignored) {
                    copyToClipboard(MONERO_ADDRESS);
                }
            }
        });
        tvAddressXmr.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                copyToClipboard(MONERO_ADDRESS);
                return true;
            }
        });

        return view;
    }

    private void copyToClipboard(String AUTHOR_EXTRA) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(getString(R.string.fragment_donate_clipboard_label), AUTHOR_EXTRA);
        clipboard.setPrimaryClip(clip);

        Toasty.success(context, R.string.fragment_donate_clipboard_message, Toast.LENGTH_SHORT, true).show();
    }

    private void openURI(String uri) {
        Intent openURI = new Intent(Intent.ACTION_VIEW);
        openURI.setData(Uri.parse(uri));
        startActivity(openURI);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        //hide action bar menu
        menu.setGroupVisible(R.id.menu_top, false);
        menu.setGroupVisible(R.id.menu_bottom, false);

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //set title
        getActivity().setTitle(getString(R.string.fragment_donate_title));
    }
}