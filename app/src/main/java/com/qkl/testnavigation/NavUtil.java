package com.qkl.testnavigation;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.AssetManager;
import android.view.Menu;
import android.view.MenuItem;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.ActivityNavigator;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavGraphNavigator;
import androidx.navigation.NavigatorProvider;
import androidx.navigation.fragment.DialogFragmentNavigator;
import androidx.navigation.fragment.FragmentNavigator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.qkl.testnavigation.model.BottomBar;
import com.qkl.testnavigation.model.Destination;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author qiukailong
 * @title:
 * @projectName NewDriverSchool
 * @description:
 * @date 2021/11/4
 */
public class NavUtil {

    private static HashMap<String, Destination> destinations;

    //读取assets目录下的文件
    public static String parseFile(Context context, String fileName) {
        AssetManager assetManager = context.getAssets();
        try {
            InputStream inputStream = assetManager.open(fileName);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            StringBuilder builder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            inputStream.close();
            bufferedReader.close();
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void builderNavGraph(FragmentActivity activity, FragmentManager childFragmentManager, NavController controller, int containerId) {
        //获取所有的节点
        destinations = JSON.parseObject(parseFile(activity, "destination.json"),
                new TypeReference<HashMap<String, Destination>>() {
                }.getType());
        Iterator<Destination> iterator = destinations.values().iterator();
        NavigatorProvider navigatorProvider = controller.getNavigatorProvider();
        NavGraphNavigator graphNavigator = navigatorProvider.getNavigator(NavGraphNavigator.class);
        NavGraph navGraph = new NavGraph(graphNavigator);
        //使用自定义的FragmentNavigator，替换内部的replace展示fragment的方式使用add和hide用来显示
        HiFragmentNavigator hiFragmentNavigator = new HiFragmentNavigator(activity, childFragmentManager, containerId);
        navigatorProvider.addNavigator(hiFragmentNavigator);
        while (iterator.hasNext()) {
            Destination destination = iterator.next();
            if (destination.destType.equals("activity")) {
                //创建activity的navigator
                ActivityNavigator navigator = navigatorProvider.getNavigator(ActivityNavigator.class);
                ActivityNavigator.Destination node = navigator.createDestination();
                node.setId(destination.id);
                node.setComponentName(new ComponentName(activity.getPackageName(), destination.clazzName));
                navGraph.addDestination(node);
            } else if (destination.destType.equals("fragment")) {
//                FragmentNavigator navigator = navigatorProvider.getNavigator(FragmentNavigator.class);
//                FragmentNavigator.Destination node = navigator.createDestination();
                HiFragmentNavigator.Destination node = hiFragmentNavigator.createDestination();
                node.setId(destination.id);
                node.setClassName(destination.clazzName);
                navGraph.addDestination(node);
            } else if (destination.destType.equals("dialog")) {
                DialogFragmentNavigator navigator = navigatorProvider.getNavigator(DialogFragmentNavigator.class);
                DialogFragmentNavigator.Destination node = navigator.createDestination();
                node.setId(destination.id);
                node.setClassName(destination.clazzName);
                navGraph.addDestination(node);
            }
            //如果是第一次启动页，就setStartDestination添加进来
            if (destination.asStarter) {
                navGraph.setStartDestination(destination.id);
            }
        }
        controller.setGraph(navGraph);
    }

    public static void builderBottomBar(BottomNavigationView navView) {
        String content = parseFile(navView.getContext(), "main_tabs_config.json");
        BottomBar bottomBar = JSON.parseObject(content, BottomBar.class);
        Menu menu = navView.getMenu();
        List<BottomBar.Tab> tabs = bottomBar.tabs;
        for (BottomBar.Tab tab : tabs) {
            if (!tab.enable) continue;
            Destination destination = destinations.get(tab.pageUrl);
            if (destination != null) {
                MenuItem menuItem = menu.add(0, destination.id, tab.index, tab.title);
                menuItem.setIcon(R.drawable.ic_home_black_24dp);
            }

        }
    }
}
