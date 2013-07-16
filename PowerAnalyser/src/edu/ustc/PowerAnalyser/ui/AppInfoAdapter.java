package edu.ustc.PowerAnalyser.ui;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class AppInfoAdapter extends SimpleAdapter{
	  
    private int[] appTo;  
    private String[] appFrom;  
    private ViewBinder appViewBinder;  
    private List<? extends Map<String, ?>>  appData;  
    private int appResource;  
    private LayoutInflater appInflater;  
      
    /**
     * 构造器
     * @param context
     * @param data
     * @param resource
     * @param from
     * @param to
     */
    public AppInfoAdapter(Context context, List<? extends Map<String, ?>> data,  
            int resource, String[] from, int[] to) {  
        super(context, data, resource, from, to);  
        appData = data;  
        appResource = resource;  
        appFrom = from;  
        appTo = to;  
        appInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);  
    }  
      
    @Override
	public View getView(int position, View convertView, ViewGroup parent){  
        return createViewFromResource(position, convertView, parent, appResource);  
    }  
      
    private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource){  
        View v;  
        
        if(convertView == null){  
            v = appInflater.inflate(resource, parent,false);  
            final int[] to = appTo;  
            final int count = to.length;  
            final View[] holder = new View[count];  
              
            for(int i = 0; i < count; i++){  
                holder[i] = v.findViewById(to[i]);  
            }  
            
            v.setTag(holder);  
        }else {  
            v = convertView;  
        }  
        
        bindView(position, v);  
        return v;     
    }  
      
    private void bindView(int position, View view){  
        final Map dataSet = appData.get(position);  
        
        if(dataSet == null){  
            return;  
        }  
          
        final ViewBinder binder = appViewBinder;  
        final View[] holder = (View[])view.getTag();  
        final String[] from = appFrom;  
        final int[] to = appTo;  
        final int count = to.length;  
          
        for(int i = 0; i < count; i++){  
            final View v = holder[i];  
            
            if(v != null){  
                final Object data = dataSet.get(from[i]);  
                String text = data == null ? "":data.toString();  
                
                if(text == null){  
                    text = "";  
                }  
                  
                boolean bound = false; 
                
                if(binder != null){  
                    bound = binder.setViewValue(v, data, text);  
                }  
                  
                if(!bound){  
                    /** 
                     * 自定义适配器，关在在这里，根据传递过来的控件以及值的数据类型， 
                     * 执行相应的方法，可以根据自己需要自行添加if语句。另外，CheckBox等 
                     * 集成自TextView的控件也会被识别成TextView，这就需要判断值的类型 
                     */  
                    if(v instanceof TextView){  
                        //如果是TextView控件，则调用SimpleAdapter自带的方法，设置文本  
                        setViewText((TextView)v, text);  
                    }else if(v instanceof ImageView){  
                        //如果是ImageView控件，调用自己写的方法，设置图片  
                        setViewImage((ImageView)v, (Drawable)data);  
                    }else {
                        throw new IllegalStateException(v.getClass().getName() + " is not a " +  
                                "view that can be bounds by this SimpleAdapter");  
                    }  
                }  
            }  
        }  
    }  
    
    public void setViewImage(ImageView v, Drawable value)  
    {  
        v.setImageDrawable(value);  
    }  


}
