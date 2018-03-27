package com.nfd.literallyrunyourcode;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import BFModel.BFModel;
import BFModel.Cell;

/**
 * @author Alexander Ronsse-Tucherov
 * @version 2018-02-02.
 * Adapter for cells such that I can display them nicely in a RecyclerView
 */
public class CellAdapter extends RecyclerView.Adapter{

    BFModel b;

    public CellAdapter(BFModel b){
        this.b = b;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View cell = inflater.inflate(R.layout.cell_view, viewGroup, false);
        return new CellHolder(cell);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
        CellHolder c = (CellHolder) viewHolder;
        TextView t = c.t;
        Cell cell = b.getCells().get(i);
        String s = cell.toString();
        if (cell == b.getCurrentCell()){
            s += "\n*";
        }
        t.setText(s);
    }

    @Override
    public int getItemCount() {
        return b.getCells().size();
    }
    private class CellHolder extends RecyclerView.ViewHolder{

        TextView t;
        public CellHolder(View itemView) {
            super(itemView);
            t = itemView.findViewById(R.id.CellText);

        }
    }
}
