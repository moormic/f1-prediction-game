package com.moormic.f1.game.model.score;

import com.moormic.f1.game.model.bonus.BonusPoint;
import lombok.Getter;

import java.util.List;

@Getter
public class TotalScore {
    private final List<Score> scores;
    private final List<BonusPoint> bonusPoints;
    private final int score;

    public TotalScore(List<Score> scores, List<BonusPoint> bonusPoints) {
        this.scores = scores;
        this.bonusPoints = bonusPoints;
        var score = scores.stream().mapToInt(Score::getScore).sum();
        var bps = bonusPoints.stream().mapToInt(BonusPoint::getPoints).sum();
        this.score = score + bps;
    }
}