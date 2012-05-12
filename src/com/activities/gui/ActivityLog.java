package com.activities.gui;

import java.util.Collections;
import java.util.List;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.model.accelerometer.R;
import com.model.database.*;

public class ActivityLog extends ListActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout. 
		DataSourceActivityEntry dataSource = new DataSourceActivityEntry(this);
		dataSource.open();
		List<ActivityEntry> pastActivities = dataSource.getAllActivities();
		dataSource.close();
		Collections.reverse(pastActivities); 
		if (pastActivities.isEmpty()) { //  if no past events 
			Toast.makeText(getApplicationContext(), "No past activities!", Toast.LENGTH_LONG).show();
		} else {
			
			setListAdapter(new ArrayAdapter<ActivityEntry>(this, R.layout.comand_item, pastActivities));
			ListView lv = getListView();
			lv.setTextFilterEnabled(true);

			lv.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// When clicked, show a toast with the TextView text
					Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
							Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
}
