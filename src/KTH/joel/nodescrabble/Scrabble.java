package KTH.joel.nodescrabble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.util.Set;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

enum AVAILABLE_PREFERENCE
{
    TEXTSIZE, SERVERIP, SERVERPORT
}

public class Scrabble extends Activity
{
    private static final int RESULT_SETTINGS = 10;
    private GameBoard board;
    private ConnectManager connectManager;
    private GameState state;
}
