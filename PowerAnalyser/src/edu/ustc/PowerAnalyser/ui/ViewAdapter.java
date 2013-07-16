package edu.ustc.PowerAnalyser.ui;

import org.taptwo.android.widget.TitleProvider;

import edu.ustc.PowerAnalyser.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ViewAdapter extends BaseAdapter implements TitleProvider {

        private static final int VIEW1 = 0;
        private static final int VIEW2 = 1;
        private static final int VIEW3 = 2;
        private static final int VIEW_MAX_COUNT = VIEW3 + 1;
    	private final String[] names = {"电池状态","功耗基准","数据分析"};

    private LayoutInflater mInflater;

    public ViewAdapter(Context context) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return VIEW_MAX_COUNT;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int view = getItemViewType(position);
        if (convertView == null) {
            switch (view) {

                case VIEW1:
                    convertView = mInflater.inflate(R.layout.battery_status, null);
                    break;
                case VIEW2:
                    convertView = mInflater.inflate(R.layout.show_power_profile, null);
                    break;
                case VIEW3:
                	convertView = mInflater.inflate(R.layout.analyse_report, null);
                	break;     	
            }
        }
        return convertView;
    }



    /* (non-Javadoc)
	 * @see org.taptwo.android.widget.TitleProvider#getTitle(int)
	 */
	@Override
	public String getTitle(int position) {
		return names[position];
	}

}
