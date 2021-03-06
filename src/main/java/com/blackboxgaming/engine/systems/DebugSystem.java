package com.blackboxgaming.engine.systems;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.blackboxgaming.engine.components.DebugEntity;
import com.blackboxgaming.engine.components.HUDItem;
import com.blackboxgaming.engine.components.IComponent;
import com.blackboxgaming.engine.Entity;
import java.util.ArrayList;
import java.util.List;

public class DebugSystem implements ISystem {

    private boolean work;

    private List<Entity> entityList = new ArrayList();

    public Stage stage = new Stage();
    protected BitmapFont font = new BitmapFont();
    protected StringBuilder stringBuilder = new StringBuilder();

    @Override
    public void remove(Entity entity) {
    }
    public DebugSystem() {
        this(true);
    }

    public DebugSystem(boolean work) {
        this.work = work;
    }

    @Override
    public void add(Entity e) {
        if (!entityList.contains(e)) {
            entityList.add(e);
        }
    }

    @Override
    public void update(float delta) {
        if (work) {
            stage.clear();
            Table table = new Table();
            //table.debug();
            for (Entity entity : entityList) {
//                for (IComponent component : ((DebugEntity) entity.get(DebugEntity.class)).entity.get()) {
//                    stringBuilder.setLength(0);
//                    stringBuilder.append(component);
//
//                    Label label = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
//                    label.setText(stringBuilder);
//                    label.setAlignment(Align.top | Align.left);
//                    table.add(label).fill();
//                    table.row();
//
//                }
            }
//            table.pack();
//            table.setY(Gdx.graphics.getHeight() - table.getHeight());
//            stage.addActor(table);
//            stage.draw();
        }
    }

}
