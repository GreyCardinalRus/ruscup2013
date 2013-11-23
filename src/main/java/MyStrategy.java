import model.*;

//import java.util.Random;
import java.util.LinkedList;

public final class MyStrategy implements Strategy {

	Trooper teamEnimy = null;
	Trooper myCommander = null;

	static boolean isDebugFull = true;
	static boolean isDebugMove = true;
	static boolean isDebugHeal = false;
	static boolean isDebugBonus = false;
	static boolean isDebugEnimy = false;
	static boolean isDebug = true;

	static boolean chicken_mode = true;

	static boolean moveZigZag = false;

	int nextX = 0, nextY = 0;

	private void showDebug(Move move, Trooper self, boolean levelDebug,
			String comment) {
		if (isDebugFull || levelDebug)
			System.out.println("	" + move.getAction() + " : target x="
					+ move.getX() + " y=" + move.getY() + " " + comment);

	}

	private boolean havePointsForMove(Trooper self, Game game) {
		return (self.getActionPoints() > (self.getStance() == TrooperStance.STANDING ? game
				.getStandingMoveCost()
				: (self.getStance() == TrooperStance.KNEELING ? game
						.getKneelingMoveCost() : game.getProneMoveCost())));

	}

	private boolean printPlayersInfo(World world) {

		Player[] players = world.getPlayers();
		for (int i = 0; i < players.length; i++) {
			System.out.println("Player: " + players[i].getName() + " Score: "
					+ players[i].getScore() + " crashed: "
					+ players[i].isStrategyCrashed() + " x="
					+ players[i].getApproximateX() + " Y="
					+ players[i].getApproximateY());
		}
		return true;
	}

	private boolean cellFree(int x, int y, World world) {
		if (x < 0 || y < 0 || x >= world.getWidth() || y >= world.getHeight())
			return false;
		if (world.getCells()[x][y] != CellType.FREE)
			return false;
		Trooper[] trooreps = world.getTroopers();
		for (int i = 0; i < trooreps.length; i++) {
			if (trooreps[i].getY() == y && trooreps[i].getX() == x)
				return false;
		}
		return true;
	}

	private boolean myMove(Unit target, Trooper self, World world, Move move,
			int dist, Game game, boolean fast) {
		if (null == target)
			return false;
		return myMove(target.getX(), target.getY(), self, world, move, dist,
				game, fast);

	}

	private boolean myMove(int targetX, int targetY, Trooper self, World world,
			Move move, int dist, Game game, boolean fast) {
		if (fast
				&& self.getActionPoints() >= game.getStanceChangeCost()
				&& (self.getStance() == TrooperStance.PRONE || self.getStance() == TrooperStance.KNEELING)) {
			move.setAction(ActionType.RAISE_STANCE);
			move.setX(self.getX());
			move.setY(self.getY());
			showDebug(move, self, isDebugMove, " from myMovie ");
			return true;
		}
		if (chicken_mode
				&& !fast
				&& self.getType() != TrooperType.FIELD_MEDIC
				&& self.getActionPoints() >= game.getStanceChangeCost()
				&& (self.getStance() == TrooperStance.STANDING || self
						.getStance() == TrooperStance.KNEELING)) {
			move.setAction(ActionType.LOWER_STANCE);
			move.setX(self.getX());
			move.setY(self.getY());
			showDebug(move, self, isDebugMove, " from myMovie ");
			return true;
		}

		if (!havePointsForMove(self, game)) {
			move.setAction(null);
			// if (isDebug)
			// System.out.println("!not move!-" + self.getActionPoints());
			return false;

		}
		int newX = self.getX(), newY = self.getY();

		// Если ячейка - солдат -то прокладываем маршрут к ячейке рядом с ним
		// -свободной!
		if (!cellFree(targetX, targetY, world)) {
			if (cellFree(targetX + 1, targetY, world))
				targetX = targetX + 1;
			else if (cellFree(targetX - 1, targetY, world))
				targetX = targetX - 1;
			else if (cellFree(targetX, targetY + 1, world))
				targetY = targetY + 1;
			else if (cellFree(targetX, targetY - 1, world))
				targetY = targetY - 1;
		}

		if (targetY == newY && targetX == (newX + 1)) {
			if (cellFree(newX + 1, newY, world)) {
				newX = (newX + 1);
				move.setX(newX);
				move.setY(newY);
				move.setAction(ActionType.MOVE);

				return true;
			}
			return false;
		}
		if (targetY == newY && targetX == (newX - 1)) {
			if (cellFree(newX - 1, newY, world)) {
				newX = (newX - 1);
				move.setX(newX);
				move.setY(newY);
				move.setAction(ActionType.MOVE);
				return true;
			}
			return false;
		}
		if (targetX == newX && targetY == (newY + 1)) {
			if (cellFree(newX, newY + 1, world)) {
				newY = (newY + 1);
				move.setX(newX);
				move.setY(newY);
				move.setAction(ActionType.MOVE);
				return true;
			}
			return false;
		}
		if (targetX == newX && targetY == (newY - 1)) {
			if (cellFree(newX, newY - 1, world)) {
				newY = (newY - 1);
				move.setX(newX);
				move.setY(newY);
				move.setAction(ActionType.MOVE);
				return true;
			}
			return false;
		}
		//
		// Создадим все нужные списки
		AStar aStar = new AStar(world.getWidth(), world.getHeight());
		// Заполним карту как-то клетками, учитывая преграду
		for (int x = 0; x < world.getWidth(); x++) {
			for (int y = 0; y < world.getHeight(); y++) {
				aStar.cellList.add(new Cell(x, y, !cellFree(x, y, world)));
			}
		}

		aStar.calculateRoute(new Cell(self.getX(), self.getY()), new Cell(
				targetX, targetY));

		if (isDebugMove)
			System.out.println(" -= CalcRoute=- "
					+ "from x="
					+ self.getX()
					+ " y="
					+ self.getY()
					+ " targetX="
					+ targetX
					+ " targetY="
					+ targetY
					+ (aStar.nextCell() == null ? " no route" : " next: "
							+ aStar.nextCell().x + " " + aStar.nextCell().y));

		if (aStar.nextCell() == null) {
			if (isDebugMove)
				aStar.printRoute();
			return false;
		}
		move.setX(aStar.nextCell().x);
		move.setY(aStar.nextCell().y);
		move.setAction(ActionType.MOVE);
		return true;
	}

	private Trooper defineEnimy(Trooper self, World world) {
		Trooper[] trooreps = world.getTroopers();
		Trooper myEnimy = null;
		// приоритет -медик!
		for (int i = 0; i < trooreps.length; i++) {
			if (!trooreps[i].isTeammate()
					&& trooreps[i].getHitpoints() > 0

					&& ((self.getShootingRange() >= trooreps[i]
							.getShootingRange()
							|| self.getShootingRange() > self
									.getDistanceTo(trooreps[i]) || trooreps[i]
							.getShootingRange() > self
							.getDistanceTo(trooreps[i])
							&& world.isVisible(trooreps[i].getShootingRange(),
									trooreps[i].getX(), trooreps[i].getY(),
									trooreps[i].getStance(), self.getX(),
									self.getY(), self.getStance())))) {

				myEnimy = (null == myEnimy
						|| (self.getDistanceTo(myEnimy) > self
								.getDistanceTo(trooreps[i]) && world.isVisible(
								self.getShootingRange(), self.getX(),
								self.getY(), self.getStance(),
								trooreps[i].getX(), trooreps[i].getY(),
								trooreps[i].getStance())) ? trooreps[i]
						: myEnimy);
				if (isDebugEnimy)
					System.out.println("	Enemy="
							+ trooreps[i].getType()
							+ " eX="
							+ trooreps[i].getX()
							+ " eY="
							+ trooreps[i].getY()
							+ " MyShootRange="
							+ self.getShootingRange()
							+ " dist="
							+ self.getDistanceTo(trooreps[i])
							+ " ShootRange="
							+ trooreps[i].getShootingRange()
							+ " heal="
							+ trooreps[i].getHitpoints()
							+ "%"
							+ (world.isVisible(self.getShootingRange(),
									self.getX(), self.getY(), self.getStance(),
									trooreps[i].getX(), trooreps[i].getY(),
									trooreps[i].getStance()) ? " visible"
									: " not visible"));
			}
		}
		if (isDebugEnimy && myEnimy != null)
			System.out.println("	chose Enemy="
					+ myEnimy.getType()
					+ " eX="
					+ myEnimy.getX()
					+ " eY="
					+ myEnimy.getY()
					+ " MyShootRange="
					+ self.getShootingRange()
					+ " dist="
					+ self.getDistanceTo(myEnimy)
					+ " ShootRange="
					+ myEnimy.getShootingRange()
					+ " heal="
					+ myEnimy.getHitpoints()
					+ "%"
					+ (world.isVisible(self.getShootingRange(), self.getX(),
							self.getY(), self.getStance(), myEnimy.getX(),
							myEnimy.getY(), myEnimy.getStance()) ? " visible"
							: " not visible"));
		return myEnimy;
	}

	private Trooper defineLeader(Trooper self, World world) {
		Trooper isCommander = null, isSolder = null, isScout = null, isMedic = null, isSniper = null;
		Trooper[] trooreps = world.getTroopers();
		for (int i = 0; i < trooreps.length; i++) {

			if (trooreps[i].isTeammate()) {
				if (trooreps[i].getType() == TrooperType.COMMANDER)
					isCommander = trooreps[i];
				else if (trooreps[i].getType() == TrooperType.SOLDIER)
					isSolder = trooreps[i];
				else if (trooreps[i].getType() == TrooperType.SCOUT)
					isScout = trooreps[i];
				else if (trooreps[i].getType() == TrooperType.SNIPER)
					isSniper = trooreps[i];
				else if (trooreps[i].getType() == TrooperType.FIELD_MEDIC)
					isMedic = trooreps[i];
			}

		}

		return (isCommander != null ? isCommander : (isScout != null ? isScout
				: (isSolder != null ? isSolder : (isSniper != null ? isSniper
						: (isMedic != null ? isMedic : null)))));
	}

	private Bonus defineBonus(Trooper self, World world) {
		Bonus[] bonuses = world.getBonuses();

		Bonus moveToBonus = null;
		for (int i = 0; i < bonuses.length; i++) {
			Bonus foundBonus = null;
			if (bonuses[i].getType() == BonusType.FIELD_RATION
					&& !self.isHoldingFieldRation()) {
				foundBonus = bonuses[i];
			} else if (bonuses[i].getType() == BonusType.GRENADE
					&& !self.isHoldingGrenade()) {
				foundBonus = bonuses[i];
			} else if (bonuses[i].getType() == BonusType.MEDIKIT
					&& !self.isHoldingMedikit()) {
				foundBonus = bonuses[i];
			}
			if (foundBonus == null)
				continue;
			if (foundBonus.getType() == BonusType.MEDIKIT
					|| moveToBonus == null)
				moveToBonus = foundBonus;

			if ((moveToBonus.getType() != BonusType.MEDIKIT || foundBonus
					.getType() == BonusType.MEDIKIT)
					&& self.getDistanceTo(moveToBonus) > self
							.getDistanceTo(foundBonus))
				moveToBonus = foundBonus;
		}
		return moveToBonus;
	}

	@Override
	public void move(Trooper self, World world, Game game, Move move) {

		Trooper[] trooreps = world.getTroopers();
		myCommander = defineLeader(self, world);
		boolean foundTeamEnime = false;

		for (int i = 0; i < trooreps.length && teamEnimy != null; i++) {

			if (trooreps[i].getId() == teamEnimy.getId())
				foundTeamEnime = true;
		}
		if (!foundTeamEnime)
			teamEnimy = null;
		// if (self.getId() == myCommander.getId())
		// myCommander = null;
		if (isDebug || isDebugFull)
			System.out.println(self.getType()
					+ " "
					+ self.getActionPoints()
					+ " sx="
					+ self.getX()
					+ " sy="
					+ self.getY()
					+ " Heal="
					+ self.getHitpoints()
					+ "% "
					+ " "
					+ self.getStance()

					+ (self.isHoldingFieldRation() ? " FieldRation" : "")
					+ (self.isHoldingGrenade() ? " Granade" : "")
					+ (self.isHoldingMedikit() ? " MedKit" : "")
					+ (myCommander != null ? " myCommander:"
							+ myCommander.getType() + "(" + myCommander.getX()
							+ ":" + myCommander.getY() + ")"
							+ myCommander.getHitpoints() + " aura="
							+ self.getDistanceTo(myCommander) + " of "
							+ game.getCommanderAuraRange() : "")
					+ (teamEnimy != null ? " teamEnimy:" + teamEnimy.getType()
							+ "(" + teamEnimy.getX() + ":" + teamEnimy.getY()
							+ ")" + teamEnimy.getHitpoints() + " "
							+ self.getDistanceTo(teamEnimy) : ""));
		// ////if(isDebug) printPlayersInfo(world);
		if (self.getActionPoints() < 2
				&& self.getType() != TrooperType.FIELD_MEDIC) {// game.getStandingMoveCost())
			showDebug(move, self, isDebug, " no point!");												// {
			return;
		}
		if (self.getMaximalHitpoints() * 0.95 > self.getHitpoints()
				&& self.isHoldingFieldRation()
				&& self.getActionPoints() < (self.getInitialActionPoints() - game
						.getFieldRationBonusActionPoints())
				&& self.getActionPoints() >= game.getFieldRationEatCost()) {
			move.setAction(ActionType.EAT_FIELD_RATION);
			move.setY(self.getY());
			move.setX(self.getX());
			showDebug(move, self, isDebug, "");
			return;
		}
		Trooper myEnimy = null;
		Bonus moveToBonus = null;
		// проверим - может мы кого-то ...
		for (int i = 0; i < trooreps.length; i++) {
			myEnimy = trooreps[i];

			// Или гранатой достанем!
			if (!myEnimy.isTeammate()
					&& self.isHoldingGrenade()
					&& game.getGrenadeThrowCost() <= self.getActionPoints()
					&& game.getGrenadeThrowRange() >= self
							.getDistanceTo(myEnimy)) {
				move.setAction(ActionType.THROW_GRENADE);
				move.setX(myEnimy.getX());
				move.setY(myEnimy.getY());

				showDebug(move, self, isDebugEnimy, " -!=HeadShot=!- "
						+ myEnimy.getType() + " heal=" + myEnimy.getHitpoints()
						+ "%");
				return;

			} // добъем одним выстрелом?
			if (!myEnimy.isTeammate()
					&& myEnimy.getHitpoints() > 0
					&& myEnimy.getHitpoints() <= self.getDamage()
					&& self.getDistanceTo(myEnimy) <= myEnimy
							.getShootingRange()
					&& self.getShootCost() <= self.getActionPoints()
					&& world.isVisible(self.getShootingRange(), self.getX(),
							self.getY(), self.getStance(), myEnimy.getX(),
							myEnimy.getY(), myEnimy.getStance())) {
				move.setAction(ActionType.SHOOT);
				move.setX(myEnimy.getX());
				move.setY(myEnimy.getY());

				showDebug(move, self, isDebugEnimy, " -!=HeadShot=!- "
						+ myEnimy.getType() + " heal=" + myEnimy.getHitpoints()
						+ "%");
				return;
			}

		}
		myEnimy = null;
		if (self.getMaximalHitpoints() * 0.45 > self.getHitpoints()
				&& self.isHoldingMedikit()
				&& self.getActionPoints() >= game.getMedikitUseCost()) {
			move.setAction(ActionType.USE_MEDIKIT);
			move.setY(self.getY());
			move.setX(self.getX());

			showDebug(move, self, isDebugHeal, "");
			return;
		}
		if (self.getMaximalHitpoints() * 0.95 > self.getHitpoints()
				&& self.getType() == TrooperType.FIELD_MEDIC
				&& self.getActionPoints() >= game.getFieldMedicHealCost()) {
			move.setX(self.getX());
			move.setY(self.getY());
			move.setAction(ActionType.HEAL);
			showDebug(move, self, isDebugHeal, "");
			return;
		}
		Trooper needHelp = null;
		// Player[] players = world.getPlayers();
		moveToBonus = defineBonus(self, world);
		myEnimy = defineEnimy(self, world);
		for (int i = 0; i < trooreps.length; i++) {

			// if (game.getCommanderAuraRange() > self
			// .getDistanceTo(myCommander))
			// myCommander = null;
			if (trooreps[i].isTeammate()
					&& trooreps[i].getMaximalHitpoints() * 0.90 > trooreps[i]
							.getHitpoints()
			// && (self.isHoldingMedikit() || self.getType() ==
			// TrooperType.FIELD_MEDIC)
			)
				needHelp = trooreps[i];

			if (trooreps[i].isTeammate()
					&& trooreps[i].getMaximalHitpoints() > trooreps[i]
							.getHitpoints()
					&& (trooreps[i].getY() == self.getY()
							&& Math.abs(trooreps[i].getX() - self.getX()) < 2 || trooreps[i]
							.getX() == self.getX()
							&& Math.abs(trooreps[i].getY() - self.getY()) < 2)) {
				if (self.getType() == TrooperType.FIELD_MEDIC
						&& self.getActionPoints() >= game
								.getFieldMedicHealCost()) {
					move.setAction(ActionType.HEAL);
					move.setX(trooreps[i].getX());
					move.setY(trooreps[i].getY());
					showDebug(move, self, isDebugHeal, " ");
					return;
				}
				if (self.isHoldingMedikit()
						&& self.getActionPoints() >= game.getMedikitUseCost()
						&& trooreps[i].getMaximalHitpoints() * 0.55 > trooreps[i]
								.getHitpoints()) {
					move.setAction(ActionType.USE_MEDIKIT);
					move.setX(trooreps[i].getX());
					move.setY(trooreps[i].getY());

					showDebug(move, self, isDebugHeal, " ");
					return;
				}
			}

		}
		if (null == myEnimy)
			myEnimy = teamEnimy;
		if (null == teamEnimy)
			teamEnimy = myEnimy;
		if (needHelp != null && needHelp.getId() != self.getId()
				&& self.getDistanceTo(needHelp) > 1) {

			myMove(needHelp, self, world, move, 0, game, true);
			if (move.getAction() != null)
				return;

		}
		if (myEnimy != null) {
			if (self.isHoldingFieldRation()
					&& self.getActionPoints() >= game.getFieldRationEatCost()
					&& self.getActionPoints() < (self.getInitialActionPoints() - game
							.getFieldRationBonusActionPoints())) {
				move.setAction(ActionType.EAT_FIELD_RATION);
				move.setY(self.getY());
				move.setX(self.getX());
				showDebug(move, self, isDebugEnimy, "");
				return;
			}
			if (self.isHoldingGrenade()
					&& game.getGrenadeThrowCost() <= self.getActionPoints()
					&& game.getGrenadeThrowRange() >= self
							.getDistanceTo(myEnimy)) {
				move.setAction(ActionType.THROW_GRENADE);
				move.setX(myEnimy.getX());
				move.setY(myEnimy.getY());
				showDebug(move, self, isDebugEnimy,
						" -!=Enemy " + myEnimy.getType());
				return;

			} else if (
			// (!self.isHoldingGrenade() || 9 < self.getActionPoints())&&
			self.getShootCost() <= self.getActionPoints()
					&& world.isVisible(self.getShootingRange(), self.getX(),
							self.getY(), self.getStance(), myEnimy.getX(),
							myEnimy.getY(), myEnimy.getStance())
					&& self.getShootingRange() >= self.getDistanceTo(myEnimy)) {
				move.setAction(ActionType.SHOOT);
				move.setX(myEnimy.getX());
				move.setY(myEnimy.getY());
				showDebug(
						move,
						self,
						isDebugEnimy,
						" -!=Enemy " + myEnimy.getType() + " heal="
								+ myEnimy.getHitpoints() + "%");
				return;
			} else if (self.getType() != TrooperType.FIELD_MEDIC
			// && self.getActionPoints() < 10
					&& chicken_mode
					// && 1 > (self.getDistanceTo(myEnimy) - self
					// .getShootingRange()) && 0 < (self
					// .getDistanceTo(myEnimy) - self.getShootingRange())
					// // && moveToBonus == null && (myCommander == null ||
					// myCommander
					// // .getId() == self.getId())
					// )
					// // ) {
					// // мы в шаге от врага - может лучше присесть, а не идти
					// под
					// // пули?
					// // if
					// && (1 < self.getActionPoints()
					// && self.getShootCost() > self.getActionPoints()
					&& self.getActionPoints() >= game.getStanceChangeCost()
			// && self.getShootingRange() >= self
			// .getDistanceTo(myEnimy)
			&& (self.getStance() == TrooperStance.STANDING || self.getStance() == TrooperStance.KNEELING)) {
				move.setAction(ActionType.LOWER_STANCE);
				move.setX(self.getX());
				move.setY(self.getY());
				showDebug(
						move,
						self,
						isDebugEnimy,
						" -!=Enemy " + myEnimy.getType() + " have:"
								+ self.getActionPoints() + " need:"
								+ game.getStanceChangeCost());

			} else {

				if ( (myCommander!=null ||myCommander.getId()!=self.getId())
					 && self.getType() == TrooperType.FIELD_MEDIC
					 ||
				self.getDistanceTo(myEnimy) < self.getShootingRange()
						&& !world.isVisible(self.getShootingRange(),
								self.getX(), self.getY(), self.getStance(),
								myEnimy.getX(), myEnimy.getY(),
								myEnimy.getStance()))
					return;

				myMove(myEnimy, self, world, move, 0, game, false);

				showDebug(move, self, isDebugEnimy,
						" -!=Enemy " + myEnimy.getType());
			}

			if (move.getAction() != null)
				return;

			// }
		} // else // freestyle
		{
			if (moveToBonus != null
			// && myCommander != null
			// && myCommander.getId() != self.getId()
					&& myCommander.getDistanceTo(moveToBonus) > game
							.getCommanderAuraRange()) {
				moveToBonus = null;
			}
			if (moveToBonus != null) {

				// move.setAction(ActionType.MOVE);

				myMove(moveToBonus, self, world, move, 0, game, true);
				showDebug(move, self, isDebugBonus, " -!= moveToBonus "
						+ moveToBonus.getType() + " (X=" + moveToBonus.getX()
						+ " Y=" + moveToBonus.getY() + ")");
				if (move.getAction() != null)
					return;
			} else if (myCommander != null
					&& myCommander.getId() != self.getId()) {

				// move.setAction(ActionType.MOVE);

				myMove(myCommander, self, world, move, 0, game, true);

				showDebug(move, self, isDebugMove, " -!= myCommander ");
				if (move.getAction() != null)
					return;
			}
			// get location

			if (move.getAction() == ActionType.MOVE
					&& move.getX() == self.getX() && move.getY() == self.getY()) {
				if (myCommander != null && myCommander != self) {

					move.setAction(ActionType.MOVE);
					myMove(myCommander, self, world, move, 0, game, true);
					showDebug(move, self, isDebugMove, " -!= myCommander ");
					if (move.getAction() != null)
						return;
				} // else {// if (self.getType() == TrooperType.COMMANDER) {

			}
		}
		// if(self.getType()==TrooperType.COMMANDER&&self.getActionPoints()>=game.getCommanderRequestEnemyDispositionCost())
		// {
		// move.setAction(ActionType.REQUEST_ENEMY_DISPOSITION);
		// showDebug(move, self, isDebug, " -!= myCommander ");
		// return;
		// }
		// else
		// {
		if (self.getX() < 7 && self.getY() < 7) {
			nextX = world.getWidth() - 2;
			nextY = 2;

		} else if (self.getX() < 7 && self.getY() > world.getHeight() - 7) {
			nextY = (moveZigZag ? world.getHeight() - 2 : 2);
			nextX = (moveZigZag ? 2 : world.getWidth() - 2);
		} else if (self.getX() > world.getWidth() - 7
				&& self.getY() > world.getHeight() - 7) {
			nextY = (moveZigZag ? 2 : world.getHeight() - 2);
			nextX = 2;
		} else if (self.getX() > world.getWidth() - 7 && self.getY() < 7) {
			nextY = world.getHeight() - 2;
			nextX = (moveZigZag ? 2 : world.getWidth() - 2);

		}
		// }
		if (isDebugMove)
			System.out.println("nextX =" + nextX + " nextY =" + nextY + " sX="
					+ self.getX() + " ww=" + world.getWidth() + " sY="
					+ self.getY() + " wh=" + world.getHeight() + " moveZigZag="
					+ moveZigZag);

		myMove(nextX, nextY, self, world, move, 0, game, true);
		showDebug(move, self, isDebugMove, " -!= freeStyle ");
		if (move.getAction() != null)
			return;
		// }
		if (move.getAction() == ActionType.MOVE

				&& (myCommander != null && myCommander.getId() != self.getId()
						|| !havePointsForMove(self, game) || move.getX() < 0
						|| move.getY() < 0 || !cellFree(move.getX(),
							move.getY(), world)))
			move.setAction(null);
	}
}

class Cell {
	/**
	 * Создает клетку с координатами x, y.
	 * 
	 * @param blocked
	 *            является ли клетка непроходимой
	 */
	public Cell(int x, int y, boolean blocked) {
		this.x = x;
		this.y = y;
		this.blocked = blocked;
	}

	public Cell(int x, int y) {
		this.x = x;
		this.y = y;
		this.blocked = false;
	}

	/**
	 * Функция вычисления манхеттенского расстояния от текущей клетки до finish
	 * 
	 * @param finish
	 *            конечная клетка
	 * @return расстояние
	 */
	public int mandist(Cell finish) {
		return 10 * (Math.abs(this.x - finish.x) + Math.abs(this.y - finish.y));
	}

	/**
	 * Вычисление стоимости пути до соседней клетки finish
	 * 
	 * @param finish
	 *            соседняя клетка
	 * @return 10, если клетка по горизонтали или вертикали от текущей, 14, если
	 *         по диагонали (это типа 1 и sqrt(2) ~ 1.44)
	 */
	public int price(Cell finish) {
		if (this.x == finish.x || this.y == finish.y) {
			return 10;
		} else {
			return 1000000;
		}
	}

	/**
	 * Устанавливает текущую клетку как стартовую
	 */
	public void setAsStart() {
		this.start = true;
	}

	/**
	 * Устанавливает текущую клетку как конечную
	 */
	public void setAsFinish() {
		this.finish = true;
	}

	/**
	 * Сравнение клеток
	 * 
	 * @param second
	 *            вторая клетка
	 * @return true, если координаты клеток равны, иначе - false
	 */
	public boolean equals(Cell second) {
		return (this.x == second.x) && (this.y == second.y);
	}

	/**
	 * Красиво печатаем * - путь (это в конце) + - стартовая или конечная # -
	 * непроходимая . - обычная
	 * 
	 * @return строковое представление клетки
	 */
	public String toString() {
		if (this.road) {
			return " * ";
		}
		if (this.start || this.finish) {
			return " + ";
		}
		if (this.blocked) {
			return " # ";
		}
		return " . ";
	}

	public int x = -1;
	public int y = -1;
	public Cell parent = this;
	public boolean blocked = false;
	public boolean start = false;
	public boolean finish = false;
	public boolean road = false;
	public int F = 0;
	public int G = 0;
	public int H = 0;
}

class Table<T extends Cell> {
	/**
	 * Создаем карту игры с размерами width и height
	 */
	public Table(int width, int height) {
		this.width = width;
		this.height = height;
		this.table = new Cell[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				table[i][j] = new Cell(0, 0, false);
			}
		}
	}

	/**
	 * Добавить клетку на карту
	 */
	public void add(Cell cell) {
		table[cell.x][cell.y] = cell;
	}

	/**
	 * Получить клетку по координатам x, y
	 * 
	 * @return клетка, либо фейковая клетка, которая всегда блокирована (чтобы
	 *         избежать выхода за границы)
	 */
	@SuppressWarnings("unchecked")
	public T get(int x, int y) {
		if (x < width && x >= 0 && y < height && y >= 0) {
			return (T) table[x][y];
		}
		// а разве так можно делать в Java? оО но работает оО
		return (T) (new Cell(0, 0, true));
	}

	/**
	 * Печать всех клеток поля. Красиво.
	 */
	public void printp() {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				System.out.print(this.get(j, i));
			}
			System.out.println();
			System.out.println();
		}
		System.out.println();
		System.out.println();
	}

	public int width;
	public int height;
	private Cell[][] table;
}

class AStar {
	private int width = 0;
	private int height = 0;
	public Table<Cell> cellList = null;

	private Cell start = null;
	private Cell nextCell = null;
	private Cell finish = null;
	private boolean noroute = false;

	public void setStartCell(int x, int y) {
		// Стартовая и конечная
		cellList.get(x, y).setAsStart();
		start = cellList.get(x, y);
		start.F = 10000;

	}

	public void setFinishCell(int x, int y) {
		// Стартовая и конечная
		cellList.get(x, y).setAsFinish();
		finish = cellList.get(x, y);
	}

	public Cell nextCell() {
		return nextCell;
	}

	AStar(int width, int height) {
		this.height = height;
		this.width = width;
		cellList = new Table<Cell>(this.width, this.height);

	}

	public void calculateRoute(Cell start, Cell end) {
		boolean found = false;
		nextCell = null;
		noroute = false;
		setStartCell(start.x, start.y);
		setFinishCell(end.x, end.y); // Фух, начинаем

		LinkedList<Cell> openList = new LinkedList<Cell>();
		LinkedList<Cell> closedList = new LinkedList<Cell>();
		LinkedList<Cell> tmpList = new LinkedList<Cell>();
		// 1) Добавляем стартовую клетку в открытый список.
		openList.push(start);

		// 2) Повторяем следующее:
		while (!found && !noroute) {
			// a) Ищем в открытом списке клетку с наименьшей стоимостью F.
			// Делаем ее текущей клеткой.
			Cell min = openList.getFirst();
			for (Cell cell : openList) {
				// тут я специально тестировал, при < или <= выбираются разные
				// пути,
				// но суммарная стоимость G у них совершенно одинакова. Забавно,
				// но так и должно быть.
				if (cell.F < min.F)
					min = cell;
			}

			// b) Помещаем ее в закрытый список. (И удаляем с открытого)
			closedList.push(min);
			openList.remove(min);
			// System.out.println(openList);

			// c) Для каждой из соседних 8-ми клеток ...
			tmpList.clear();
			tmpList.add(cellList.get(min.x, min.y - 1));
			tmpList.add(cellList.get(min.x + 1, min.y));
			tmpList.add(cellList.get(min.x, min.y + 1));
			tmpList.add(cellList.get(min.x - 1, min.y));

			for (Cell neightbour : tmpList) {
				// Если клетка непроходимая или она находится в закрытом списке,
				// игнорируем ее. В противном случае делаем следующее.
				if (neightbour.blocked || closedList.contains(neightbour))
					continue;

				// Если клетка еще не в открытом списке, то добавляем ее туда.
				// Делаем текущую клетку родительской для это клетки.
				// Расчитываем стоимости F, G и H клетки.
				if (!openList.contains(neightbour)) {
					openList.add(neightbour);
					neightbour.parent = min;
					neightbour.H = neightbour.mandist(finish);
					neightbour.G = neightbour.price(min);
					neightbour.F = neightbour.H + neightbour.G;
					continue;
				}
				// Если клетка уже в открытом списке, то проверяем, не дешевле
				// ли будет путь через эту клетку. Для сравнения используем
				// стоимость G.
				if (neightbour.F < min.F // + neightbour.price(min)
				) {
					// Более низкая стоимость G указывает на то, что путь будет
					// дешевле. Эсли это так, то меняем родителя клетки на
					// текущую клетку и пересчитываем для нее стоимости G и F.
					neightbour.parent = min.parent; // вот тут я честно хз, надо
													// ли min.parent или нет,
													// вроде надо
					neightbour.H = neightbour.mandist(finish);
					neightbour.G = neightbour.price(min);
					neightbour.F = neightbour.H + neightbour.G;
				}

				// Если вы сортируете открытый список по стоимости F, то вам
				// надо отсортировать свесь список в соответствии с изменениями.
			}

			// d) Останавливаемся если:
			// Добавили целевую клетку в открытый список, в этом случае путь
			// найден.
			// Или открытый список пуст и мы не дошли до целевой клетки. В этом
			// случае путь отсутствует.

			if (openList.contains(finish)) {
				found = true;
			}

			if (openList.isEmpty()) {
				noroute = true;
			}
		}

		// 3) Сохраняем путь. Двигаясь назад от целевой точки, проходя от каждой
		// точки к ее родителю до тех пор, пока не дойдем до стартовой точки.
		// Это и будет наш путь.
		if (!noroute) {
			Cell rd = finish.parent;
			while (!rd.equals(start)) {
				rd.road = true;
				nextCell = rd;
				rd = rd.parent;
				if (rd == null)
					break;
			}
			// cellList.printp();
		} else {
			System.out.println("NO ROUTE");
		}

	}

	public void printRoute() {
		cellList.printp();
		if (!noroute) {

		} else {
			System.out.println("NO ROUTE");
		}

	}

	public static void main(String[] args) {
		// Создадим все нужные списки
		AStar aStar = new AStar(10, 10);
		Table<Cell> blockList = new Table<Cell>(10, 10);
		// Создадим преграду
		blockList.add(new Cell(3, 2, true));
		blockList.add(new Cell(5, 2, true));
		blockList.add(new Cell(4, 2, true));
		blockList.add(new Cell(4, 3, true));
		blockList.add(new Cell(4, 4, true));
		blockList.add(new Cell(4, 5, true));
		blockList.add(new Cell(4, 6, true));
		blockList.add(new Cell(4, 7, true));
		blockList.add(new Cell(3, 7, true));
		// blockList.add(new Cell(2, 7, true));
		blockList.add(new Cell(1, 7, true));
		blockList.add(new Cell(0, 7, true));
		// Заполним карту как-то клетками, учитывая преграду
		for (int i = 0; i < aStar.width; i++) {
			for (int j = 0; j < aStar.height; j++) {
				aStar.cellList.add(new Cell(j, i, blockList.get(j, i).blocked));
			}
		}

		aStar.calculateRoute(new Cell(2, 4), new Cell(7, 8));
		aStar.printRoute();
		System.out.println(aStar.nextCell().x + " " + aStar.nextCell().y);

	}
}