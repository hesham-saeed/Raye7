package com.apps.hesham.raye7;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.apps.hesham.raye7.DirectionsAPI.DirectionFinder;
import com.apps.hesham.raye7.DirectionsAPI.Route;

/**
 * Created by Hesham Sa'eed on 08/06/2017.
 */

public class RoutePickerFragment extends DialogFragment {
    private RecyclerView mRouteRecyclerView;
    private RouteAdapter mRouteAdapter;

    private List<Route> routes;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_route_picker, null);
        mRouteRecyclerView = (RecyclerView) v.findViewById(R.id.route_picker_recycler_view);
        mRouteRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        routes = DirectionFinder.routes;
        mRouteAdapter = new RouteAdapter(routes);
        mRouteRecyclerView.setAdapter(mRouteAdapter);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(R.string.route_picker_title)
                .create();

    }

    private class RouteHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView duration;
        TextView routeName;
        TextView distance;
        TextView position;
        public RouteHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            duration = (TextView) itemView.findViewById(R.id.durationTextView);
            routeName = (TextView) itemView.findViewById(R.id.routeNameTextView);
            position = (TextView) itemView.findViewById(R.id.posTextView);
            distance = (TextView) itemView.findViewById(R.id.distanceTextView);
        }

        @Override
        public void onClick(View v) {
            int Pos = Integer.parseInt(
                    ((TextView)v.findViewById(R.id.posTextView)).getText().toString());
            DirectionFinder.routes.get(Pos).setChosen(true);
            mListener.onComplete();
            dismiss();
        }
    }


    private class RouteAdapter extends RecyclerView.Adapter<RouteHolder>{
        private List<Route> mRoutes;

        public RouteAdapter(List<Route> routes){
            mRoutes = routes;
            findStreetNames();
        }
        public RouteHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.route_item_view,parent,false);
            return new RouteHolder(v);
        }
        public void onBindViewHolder(RouteHolder holder, int position) {
            holder.distance.setText(mRoutes.get(position).getDistance().getText());
            holder.duration.setText(mRoutes.get(position).getDuration().getText());
            holder.routeName.setText("via " + mRoutes.get(position).getRouteName());
            holder.position.setText(String.valueOf(position));
        }
        public int getItemCount() {
            return mRoutes.size();
        }
    }
    public interface OnCompleteListener {
        public abstract void onComplete();
    }
    private OnCompleteListener mListener;


    private class RouteDistance{
        Float distance;
        LatLng p1;
        LatLng p2;
    }

    private void findStreetNames(){

        List<List<RouteDistance>> routeDistances = new ArrayList<>();

        for (int i=0;i<routes.size();i++){
            List<LatLng> points = routes.get(i).getPoints();

            List<RouteDistance> routeDistance = new ArrayList<RouteDistance>();
            for (int j=1;j<points.size();j++){
                RouteDistance item = new RouteDistance();
                float [] dist = new float[1];
                Location.distanceBetween(
                        points.get(j).latitude,
                        points.get(j).longitude,
                        points.get(j-1).latitude,
                        points.get(j-1).longitude,
                        dist);
                item.distance = dist[0];
                item.p1 = points.get(j);
                item.p2 = points.get(j-1);
                routeDistance.add(item);
            }
            Collections.sort(routeDistance, new Comparator<RouteDistance>() {
                @Override
                public int compare(RouteDistance o1, RouteDistance o2) {
                    if (o1.distance < o2.distance)
                        return 1;
                    else if (o1.distance > o2.distance)
                        return -1;
                    return 0;
                }
            });
            routeDistances.add(routeDistance);
        }
        int min = routes.get(0).getPoints().size();
        for (int i=1; i<routes.size();i++)
        {
            if (routes.get(i).getPoints().size()< min)
                min = routes.get(i).getPoints().size();
        }
        for (int i=0; i<min; i++){
            boolean found = true;
            for (int j=1; j<routeDistances.size(); j++){
                if (routeDistances.get(j).get(i).distance.equals(routeDistances.get(j-1).get(i).distance))
                    found = false;
                if ((routeDistances.get(j).get(i).p2.equals(routeDistances.get(j-1).get(i).p1) &&
                        routeDistances.get(j).get(i).p2.equals(routeDistances.get(j-1).get(i).p2)) ||
                        (routeDistances.get(j).get(i).p1.equals(routeDistances.get(j-1).get(i).p1) &&
                                routeDistances.get(j).get(i).p1.equals(routeDistances.get(j-1).get(i).p2))){
                    found = false;
                }

            }
            if (found == true){
                for (int j=0;j<routeDistances.size();j++){
                    LatLng streetLatLng = routeDistances.get(j).get(i).p2;
                    Geocoder coder = new Geocoder(getContext());
                    List<Address> addressList;
                    try {
                        addressList = coder.getFromLocation(streetLatLng.latitude,
                                streetLatLng.longitude,1);
                        routes.get(j).setRouteName(addressList.get(0).getAddressLine(0));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                Set<String> stringSet = new HashSet<>();
                for (int k=0;k<routeDistances.size();k++){
                    stringSet.add(routes.get(k).getRouteName());
                }
                if (stringSet.size() < routes.size())
                    found = false;
                else
                {
                    for (int k=0;k<routes.size();k++){
                        routes.get(k).setRouteName(routes.get(k).getRouteName().replaceAll("[*0-9]", ""));
                    }
                    break;
                }
            }
        }
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        FragmentActivity activity;

        if (context instanceof FragmentActivity){
            activity =(FragmentActivity) context;
            try {
                this.mListener = (OnCompleteListener)activity;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
