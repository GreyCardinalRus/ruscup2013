import model.*;

import java.util.Random;

public final class MyStrategy implements Strategy {
	// private final Random random = new Random();
	Bonus moveToBonus = null;
	Trooper myEnimy = null;
	Trooper myGeneral = null;

	private boolean myMove(Unit target, Trooper self, World world, Move move) {
		if(null==target) return false;
		return myMove(target.getX(), target.getY(), self, world, move);

	}

	private boolean myMove(int targetX, int targetY, Trooper self, World world,
			Move move) {
		int newX = self.getX(), newY = self.getY();
		boolean Xfree = true, Yfree = true;
		if (targetY != newY) {
			for (int iy = (targetY < newY ? targetY : newY); iy < (targetY > newY ? targetY
					: newY); iy++) {
				if (world.getCells()[targetX][iy] != CellType.FREE)
					Yfree = false;
			}
		}
		if (targetX != newX) {
			for (int ix = (targetX < newX ? targetX : newX); ix < (targetX > newX ? targetX
					: newX); ix++) {
				if (world.getCells()[ix][targetY] != CellType.FREE)
					Xfree = false;
			}
		}
		if (Xfree && Yfree) {
			// System.out.println("-=!=- target : " +
			// " X="+target.getX()+" Y="+target.getY()+" id="+target.getId());
			if (targetY != self.getY()) {
				newY += (targetY < self.getY() ? -1 : 1);
				if (world.getCells()[newX][newY] != CellType.FREE)
					newY = self.getY();
			}

			if (newY == self.getY() && targetX != self.getX()) {
				newX += targetX < self.getX() ? -1 : 1;
				if (world.getCells()[newX][newY] != CellType.FREE)
					newX = self.getX();
			}
		} else if (Xfree) {
			if (targetX != self.getX()) {
				newX += targetX < self.getX() ? -1 : 1;
				if (world.getCells()[newX][newY] != CellType.FREE)
					newX = self.getX();
			}
			if (newX == self.getX() && targetY != self.getY()) {
				newY += (targetY < self.getY() ? -1 : 1);
				if (world.getCells()[newX][newY] != CellType.FREE)
					newY = self.getY();
			}

		} else if (Yfree) {
			if (targetY != self.getY()) {
				newY += (targetY < self.getY() ? -1 : 1);
				if (world.getCells()[newX][newY] != CellType.FREE)
					newY = self.getY();
			}

			if (newY == self.getY() && targetX != self.getX()) {
				newX += targetX < self.getX() ? -1 : 1;
				if (world.getCells()[newX][newY] != CellType.FREE)
					newX = self.getX();
			}
		}

		move.setX(newX);
		move.setY(newY);
		if (targetY == newY && targetX == newX)
			return false;
		return true;
	}

	@Override
	public void move(Trooper self, World world, Game game, Move move) {
		if (self.getActionPoints() < 1) {// game.getStandingMoveCost()) {
			moveToBonus = null;
			return;
		}
		if (self.getMaximalHitpoints() > self.getHitpoints()
				&& self.isHoldingMedikit()
				&& self.getActionPoints() > game.getMedikitUseCost()) {
			move.setAction(ActionType.USE_MEDIKIT);
			return;
		}

		Trooper[] trooreps = world.getTroopers();
		// Player[] players = world.getPlayers();
		Bonus[] bonuses = world.getBonuses();

		boolean haveVisibleenemys = false;

		// System.out.println("-=Why I am = " +
		// self.getType()+" X="+self.getX()+" Y="+self.getY() + " : "
		// + self.getActionPoints());
		if (moveToBonus == null)
			for (int i = 0; i < bonuses.length; i++) {
				Bonus foundBonus = null;
				// System.out.println(bonuses[i].getType());
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
				if (foundBonus != null && moveToBonus == null)
					moveToBonus = foundBonus;
				else if (foundBonus == null)
					;
				else if (self.getDistanceTo(moveToBonus) > self
						.getDistanceTo(foundBonus))
					moveToBonus = foundBonus;
			}
		for (int i = 0; i < trooreps.length; i++) {
			// if (players[pid].getId()==trooreps[i].getPlayerId())

			if (trooreps[i].isTeammate() && trooreps[i].getId() != self.getId()
					&& trooreps[i].getType() == TrooperType.COMMANDER) {
				myGeneral = trooreps[i];
			}

			if (!trooreps[i].isTeammate()) {
				// haveVisibleenemys = true;
				myEnimy = trooreps[i];
				System.out.println("Enemy=" + trooreps[i].getType());
			}

		}
		if (myEnimy != null) {
			if (self.getShootCost() < self.getActionPoints()
					&& world.isVisible(self.getShootingRange(), self.getX(),
							self.getY(), self.getStance(), myEnimy.getX(),
							myEnimy.getY(), myEnimy.getStance())) {
				move.setAction(ActionType.SHOOT);
				move.setX(myEnimy.getX());
				move.setY(myEnimy.getY());
			} else {
				move.setAction(ActionType.MOVE);
				myMove(myEnimy, self, world, move);
				myEnimy = null;
			}
		} else // freestyle
		{

			if (!(moveToBonus == null)) {
				move.setAction(ActionType.MOVE);

				myMove(moveToBonus, self, world, move);
				moveToBonus = null;

			} else if (myGeneral != null) {
				move.setAction(ActionType.MOVE);
				// myGeneral = (Trooper)
				myMove(myGeneral, self, world, move);
			}
			// get location

		}
		if (move.getAction() == ActionType.MOVE && move.getX() == self.getX()
				&& move.getY() == self.getY()) {
			if (myGeneral != null) {
				move.setAction(ActionType.MOVE);
				// myGeneral = (Trooper)
				myMove(myGeneral, self, world, move);
			} else if (self.getType() == TrooperType.COMMANDER) {
				move.setAction(ActionType.MOVE);
				// myGeneral = (Trooper)
				myMove(myGeneral, self, world, move);
			}
		}
	}
}
