package com.sikderithub.keyboard.Adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.android.inputmethod.keyboard.KeyboardTheme;
import com.google.gson.Gson;
import com.sikderithub.keyboard.R;
import com.sikderithub.keyboard.customView.CircleView;

public class ThemeListAdapter extends RecyclerView.Adapter<ThemeListAdapter.MyViewHolder> {
    private static final String TAG="ThemeListAdapter";
    private int selectedThemeId;
    private ThemeAdapter.DefaultThemeSelectListener defaultThemeSelectListener;
    private int columnWidth;
    Context context;
    KeyboardTheme[] keyboardThemes;

    public interface DefaultThemeSelectListener{
        void onSelect(int themeId);
    }

    public void setDefaultThemeSelectListener(ThemeAdapter.DefaultThemeSelectListener defaultThemeSelectListener) {
        this.defaultThemeSelectListener = defaultThemeSelectListener;
    }
    public ThemeListAdapter(Context context, KeyboardTheme[] keyboardThemes) {
        this.context = context;
        this.keyboardThemes = keyboardThemes;
        selectedThemeId = KeyboardTheme.getKeyboardTheme(context).mThemeId;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ThemeListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_theme_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ThemeListAdapter.MyViewHolder holder, int position) {
        KeyboardTheme theme = keyboardThemes[position];
        ContextThemeWrapper mThemeContext = new ContextThemeWrapper(context, theme.mStyleId);

        // Resolve the keyboardStyle attribute from the theme
        TypedValue typedValue = new TypedValue();
        mThemeContext.getTheme().resolveAttribute(R.attr.keyboardViewStyle, typedValue, true);

        // Check if the attribute is a resource reference
        if (typedValue.type == TypedValue.TYPE_REFERENCE) {
            int keyboardStyleResId = typedValue.resourceId;

            // Obtain a TypedArray for the keyboardStyle attributes
            TypedArray keyboardStyleTypedArray = mThemeContext.obtainStyledAttributes(keyboardStyleResId,
                    new int[] { android.R.attr.background, R.attr.keyBackground, R.attr.functionalKeyBackground, R.attr.keyTextColor });

            // Retrieve the background drawable resource ID from the TypedArray
            int backgroundDrawableResId = keyboardStyleTypedArray.getResourceId(0, 0);
            int spaceBackgroundDrawableResId = keyboardStyleTypedArray.getResourceId(1, 0);
            int funBackgroundDrawableResId = keyboardStyleTypedArray.getResourceId(2, 0);


            holder.txtKey.setTextColor(keyboardStyleTypedArray.getColor(3, 0));

            if(funBackgroundDrawableResId!=0){
                Drawable funBackgroundDrawable = mThemeContext.getDrawable(funBackgroundDrawableResId);
                holder.circleView.setBackground(funBackgroundDrawable);

            }else{
                Log.d(TAG, "funBackgroundDrawableResId: "+position+" "+funBackgroundDrawableResId);
            }

            // Check if the background drawable resource ID is valid
            if (backgroundDrawableResId != 0) {
                // Load the background drawable from the themed context resources
                Drawable backgroundDrawable = mThemeContext.getResources().getDrawable(backgroundDrawableResId);
                Drawable spaceBackgroundDrawable = mThemeContext.getResources().getDrawable(spaceBackgroundDrawableResId);

                // Now you can use the background drawable as needed
                // For example, set the background of a view to the retrieved drawable
                holder.container.setBackground(backgroundDrawable);
                holder.cvSpace.setBackground(spaceBackgroundDrawable);

                // Recycle the TypedArray to avoid memory leaks
                keyboardStyleTypedArray.recycle();
            }
    }
        holder.txtName.setText(theme.mThemeName);

        if(keyboardThemes[position].mThemeId==selectedThemeId){
            holder.imgSelected.setVisibility(View.VISIBLE);
        }else{
            holder.imgSelected.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        return keyboardThemes.length;
    }

    public void setColumnWidth(int columnWidth) {
        this.columnWidth = columnWidth-10;
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout container;
        TextView txtName, txtKey;
        CardView cvSpace;
        CircleView circleView;
        ImageView imgSelected;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.container);
            txtName = itemView.findViewById(R.id.txtName);
            cvSpace = itemView.findViewById(R.id.cvSpace);
            circleView = itemView.findViewById(R.id.circleView);
            txtKey = itemView.findViewById(R.id.txtKey);
            imgSelected = itemView.findViewById(R.id.imgSelected);

            itemView.setOnClickListener(view -> {
                if(defaultThemeSelectListener!=null){
                    int pos = getAdapterPosition();
                    if (pos>=0){
                        selectedThemeId = keyboardThemes[pos].mThemeId;

                        if(defaultThemeSelectListener!=null){
                            defaultThemeSelectListener.onSelect(selectedThemeId);
                            notifyDataSetChanged();
                        }
                    }
                }
            });
        }

    }
}
