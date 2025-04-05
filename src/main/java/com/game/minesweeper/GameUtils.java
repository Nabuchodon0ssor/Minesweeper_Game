package com.game.minesweeper;

import java.util.ArrayList;
import java.util.List;


public class GameUtils {

    public static List<GameObject> getNeighbors(GameObject gameObject, GameObject[][] gameField, int side) {
        List<GameObject> result = new ArrayList<>();
        for (int y = gameObject.y - 1; y <= gameObject.y + 1 ; y++) {
            for (int x = gameObject.x - 1; x <= gameObject.x + 1 ; x++) {
                if (y < 0 || y >= side) {
                    continue;
                }
                if (x < 0 || x >= side) {
                    continue;
                }
                if (gameField[y][x] == gameObject) {
                    continue;
                }
                result.add(gameField[y][x]);
            }
        }
        return result;
    }

}
