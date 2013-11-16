import model.*;

import java.util.Random;

public final class MyStrategy implements Strategy {
	// private final Random random = new Random();
	Bonus moveToBonus = null;
	Trooper myEnimy = null;
	Trooper myCommander = null;
	Trooper needHeal = null;
	Trooper[] trooreps = null;
	World world = null;
	Trooper self = null;
	static final boolean isDebug = false;
	static final boolean isDebugFull = false;
	int nextX = 0, nextY = 0;
    private void showDebug(Move move, Trooper self, boolean levelDebug,String comment)
    {
		if(levelDebug)System.out.println("-=!=- "+ self.getType()
				 + " sx=" + self.getX() + " sy="
				+ self.getY() + " self.getActionPoints()="
				+ self.getActionPoints() + " "+move.getAction()+" : target x="+move.getX()+" y="+move.getY()+" "+comment);

    	
    }
	private boolean cellFree(int x, int y, World world) {
		if (x < 0 || y < 0 || x >= world.getWidth() || y >= world.getHeight())
			return false;
		if (world.getCells()[x][y] != CellType.FREE)
			return false;
		Trooper[] trooreps = world.getTroopers();
		for (int i = 0; i < trooreps.length; i++) {
			if (trooreps[i].getId() != self.getId() && trooreps[i].getY() == y
					&& trooreps[i].getX() == x)
				return false;
		}
		return true;
	}

	private boolean myMove(Unit target, Trooper self, World world, Move move,
			int dist,Game game) {
		if (null == target)
			return false;
		return myMove(target.getX(), target.getY(), self, world, move, dist,game);

	}

	private boolean myMove(int targetX, int targetY, Trooper self, World world,
			Move move, int dist,Game game) {
		if(self.getActionPoints()<game.getStandingMoveCost()){
			move.setAction(null);
			return false;
		}
		/*
		 * dir n==near f==far t==target b==bonus m==medic
		 */
		int newX = self.getX(), newY = self.getY();
		// if(self.getDistanceTo(newX, newY)<(dist+2) &&self.getDistanceTo(newX,
		// newY)>(dist-2)) return false;
		if (dist > 2 && self.getDistanceTo(newX, newY) < (dist + 2)) {
			if (newX < targetX && cellFree(newX - 1, newY, world))
				newX--;
			else if (newX > targetX && cellFree(newX + 1, newY, world))
				newX++;
			else if (newY < targetY && cellFree(newX, newY - 1, world))
				newY--;
			else if (newY > targetY && cellFree(newX, newY + 1, world))
				newY++;
			return true;
		}

		boolean Xfree = true, Yfree = true;
		if (targetY != newY) {
			for (int iy = (targetY < newY ? targetY : newY); iy < (targetY > newY ? targetY
					: newY); iy++) {
				if (!cellFree(targetX, iy, world))
					Yfree = false;
			}
		}
		if (targetX != newX) {
			for (int ix = (targetX < newX ? targetX : newX); ix < (targetX > newX ? targetX
					: newX); ix++) {
				if (!cellFree(ix, targetY, world))
					Xfree = false;
			}
		}
		// if (isDebug) System.out.println("-=!=- target : " +
		// " X="+targetX+" Y="+targetY+" id=");
		if (Xfree && Yfree) {
			// if (isDebug) System.out.println("-=!=- target : " +
			// " X="+target.getX()+" Y="+target.getY()+" id="+target.getId());
			if (targetY != self.getY()) {
				newY += (targetY < self.getY() ? -1 : 1);
				if (!cellFree(newX, newY, world))
					newY = self.getY();
			}

			if (newY == self.getY() && targetX != self.getX()) {
				newX += targetX < self.getX() ? -1 : 1;
				if (!cellFree(newX, newY, world))
					newX = self.getX();
			}
		} else if (Xfree) {
			if (targetX != self.getX()) {
				newX += targetX < self.getX() ? -1 : 1;
				if (!cellFree(newX, newY, world))
					newX = self.getX();
			}
			if (newX == self.getX() && targetY != self.getY()) {
				newY += (targetY < self.getY() ? -1 : 1);
				if (!cellFree(newX, newY, world))
					newY = self.getY();
			}

		} else if (Yfree) {
			if (targetY != self.getY()) {
				newY += (targetY < self.getY() ? -1 : 1);
				if (!cellFree(newX, newY, world))
					newY = self.getY();
			}

			if (newY == self.getY() && targetX != self.getX()) {
				newX += targetX < self.getX() ? -1 : 1;
				if (!cellFree(newX, newY, world))
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
		//if(isDebug) System.out.println(world.);
		if (self.getActionPoints() < 1) {// game.getStandingMoveCost()) {
			moveToBonus = null;
			return;
		}
		if (self.getMaximalHitpoints() > self.getHitpoints()
				&& self.isHoldingFieldRation()
				&& self.getActionPoints() > game.getFieldRationEatCost()) {
			move.setAction(ActionType.EAT_FIELD_RATION);
			move.setY(self.getY());
			move.setX(self.getX());
			showDebug(move,self,isDebugFull,"");
//			if (isDebugFull)
//				System.out.println("-=!=- ActionType.EAT_FIELD_RATION : "
//						+ self.getType() + " sx=" + self.getX() + " sy="
//						+ self.getY() + " self.getActionPoints()="
//						+ self.getActionPoints() + " game.getActionPoints()="
//						+ game.getFieldRationEatCost());
			return;
		}
		myEnimy = null;
		if (self.getMaximalHitpoints() > self.getHitpoints()
				&& self.isHoldingMedikit()
				&& self.getActionPoints() > game.getMedikitUseCost()) {
			move.setAction(ActionType.USE_MEDIKIT);
			move.setY(self.getY());
			move.setX(self.getX());

			showDebug(move,self,isDebugFull,"");
//			if (isDebugFull)
//				System.out.println("-=!=- ActionType.USE_MEDIKIT : "
//						+ self.getType() + " sx=" + self.getX() + " sy="
//						+ self.getY() + " self.getActionPoints()="
//						+ self.getActionPoints() + " game.getMedikitUseCost()="
//						+ game.getMedikitUseCost());
			return;
		}
		if (self.getMaximalHitpoints() > self.getHitpoints()
				&& self.getType() == TrooperType.FIELD_MEDIC
				&& self.getActionPoints() > game.getFieldMedicHealCost()) {
			move.setX(self.getX());
			move.setY(self.getY());
			move.setAction(ActionType.HEAL);
			showDebug(move,self,isDebugFull,"");
//			if (isDebugFull)
//				System.out.println("-=!=- ActionType.HEAL : "
//						+ self.getType() + " sx=" + self.getX() + " sy="
//						+ self.getY() + " self.getActionPoints()="
//						+ self.getActionPoints() + " game.getMedikitUseCost()="
//						+ game.getMedikitUseCost());
			return;
		}
		this.world = world;
		this.self = self;
		trooreps = world.getTroopers();
		// Player[] players = world.getPlayers();
		Bonus[] bonuses = world.getBonuses();
		needHeal = null;
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
		for (int i = 0; i < trooreps.length; i++) {

			if (trooreps[i].isTeammate() && trooreps[i].getId() != self.getId()
					&& trooreps[i].getType() == TrooperType.COMMANDER) {
				myCommander = trooreps[i];
				if (game.getCommanderAuraRange() > self
						.getDistanceTo(myCommander))
					myCommander = null;
			}
			if (trooreps[i].isTeammate()
					&& trooreps[i].getMaximalHitpoints() > trooreps[i]
							.getHitpoints()
					&& (self.isHoldingMedikit() || self.getType() == TrooperType.FIELD_MEDIC))
				needHeal = trooreps[i];

			if (!trooreps[i].isTeammate() && trooreps[i].getHitpoints() > 0) {
				if (null == myEnimy
						|| myEnimy.getShootingRange() < trooreps[i]
								.getShootingRange())
					myEnimy = (null == myEnimy
							|| self.getDistanceTo(myEnimy) < self
									.getDistanceTo(trooreps[i]) ? trooreps[i]
							: myEnimy);
//				showDebug(move,self,isDebug," -!=Enemy ");
				if (isDebug)
					System.out.println("Enemy=" + trooreps[i].getType()
							+ " eX=" + trooreps[i].getX() + " eY="
							+ trooreps[i].getY());
			}
			if (trooreps[i].isTeammate()
					&& trooreps[i].getMaximalHitpoints() > trooreps[i]
							.getHitpoints()
					&& (trooreps[i].getY() == self.getY()
							&& Math.abs(trooreps[i].getX() - self.getX()) < 2 || trooreps[i]
							.getX() == self.getX()
							&& Math.abs(trooreps[i].getY() - self.getY()) < 2)) {
				if (self.getType() == TrooperType.FIELD_MEDIC
						&& self.getActionPoints() > game
								.getFieldMedicHealCost()) {
					move.setAction(ActionType.HEAL);
					move.setX(trooreps[i].getX());
					move.setY(trooreps[i].getY());
					showDebug(move,self,isDebugFull," ");
					/*
					 * if (isDebug)
					 * System.out.println("-=!=- ActionType.HEAL : " +
					 * self.getType() + " sx=" + self.getX() + " sy=" +
					 * self.getY());
					 */
					return;
				}
				if (self.isHoldingMedikit()
						&& self.getActionPoints() > game.getMedikitUseCost()) {
					move.setAction(ActionType.USE_MEDIKIT);
					move.setX(trooreps[i].getX());
					move.setY(trooreps[i].getY());

					showDebug(move,self,isDebugFull," ");
//					if (isDebugFull)
//						System.out.println("-=!=- ActionType.USE_MEDIKIT : "
//								+ self.getType() + " sx=" + self.getX()
//								+ " sy=" + self.getY()
//								+ " self.getActionPoints()="
//								+ self.getActionPoints()
//								+ " game.getMedikitUseCost()="
//								+ game.getMedikitUseCost());

					return;
				}
			}

		}
		if (needHeal != null) {
			move.setAction(ActionType.MOVE);

			needHeal = (myMove(needHeal, self, world, move, 0,game) ? needHeal
					: null);
			

		} else if (myEnimy != null) {
			if(self.isHoldingGrenade()&&game.getGrenadeThrowCost() < self.getActionPoints()
					&& game.getGrenadeThrowRange()>self.getDistanceTo(myEnimy)){
				move.setAction(ActionType.THROW_GRENADE);
				move.setX(myEnimy.getX());
				move.setY(myEnimy.getY());
				showDebug(move,self,isDebug," -!=Enemy ");
//				if (isDebug)
//					System.out.println("-=!=- ActionType.THROW_GRENADE : " + " X="
//							+ move.getX() + " Y=" + move.getY() + " "
//							+ self.getType() + " sx=" + self.getX() + " sy="
//							+ self.getY());
			
				
			}
			else if (self.getShootCost() < self.getActionPoints()
					&& world.isVisible(self.getShootingRange(), self.getX(),
							self.getY(), self.getStance(), myEnimy.getX(),
							myEnimy.getY(), myEnimy.getStance())
							&&self.getShootingRange()>self.getDistanceTo(myEnimy)) {
				move.setAction(ActionType.SHOOT);
				move.setX(myEnimy.getX());
				move.setY(myEnimy.getY());
				showDebug(move,self,isDebug," -!=Enemy ");
				return;
			}else if(self.getShootingRange()>self.getDistanceTo(myEnimy))	{
				///if(self.getStance())
				move.setAction(null);
				return;
			} else {
				move.setAction(ActionType.MOVE);

				myEnimy = (myMove(
						myEnimy,
						self,
						world,
						move,
						 (int) self.getShootingRange(),game) ? myEnimy
						: null);
				
				showDebug(move,self,isDebug," -!=Enemy ");
				
				return;

			}
		} else // freestyle
		{

			if (moveToBonus != null) {
			
//				if (isDebug)
//					System.out.println("-=!=- moveToBonus : " + " X="
//							+ moveToBonus.getX() + " Y=" + moveToBonus.getY()
//							+ " " + self.getType() + " sx=" + self.getX()
//							+ " sy=" + self.getY());
				move.setAction(ActionType.MOVE);

				moveToBonus = (myMove(moveToBonus, self, world, move, 0,game) ? moveToBonus
						: null);
				
				showDebug(move,self,isDebug," -!= moveToBonus ");
				} else if (myCommander != null) {
//				if (isDebug)
//					System.out.println("-=!=- myCommander : " + " X="
//							+ myCommander.getX() + " Y=" + myCommander.getY()
//							+ " " + self.getType() + " sx=" + self.getX()
//							+ " sy=" + self.getY());
				move.setAction(ActionType.MOVE);

				myCommander = (myMove(myCommander, self, world, move, 0,game) ? myCommander
						: null);
				
				showDebug(move,self,isDebug," -!= myCommander ");
			}
			// get location

			if (move.getAction() == ActionType.MOVE
					&& move.getX() == self.getX() && move.getY() == self.getY()) {
				if (myCommander != null) {
//					if (isDebug)
//						System.out.println("-=!=- myCommander : " + " X="
//								+ myCommander.getX() + " Y="
//								+ myCommander.getY() + " " + self.getType()
//								+ " sx=" + self.getX() + " sy=" + self.getY());
					move.setAction(ActionType.MOVE);
					myCommander = (myMove(myCommander, self, world, move, 0,game) ? myCommander
							: null);
					showDebug(move,self,isDebug," -!= myCommander ");
				} else {// if (self.getType() == TrooperType.COMMANDER) {
						// if (nextY == 0)
					{// init1
						if (self.getX() < world.getWidth() / 2
								&& self.getY() < world.getHeight() / 2) {
							nextY = 2;
							nextX = world.getWidth() - 2;
						} else if (self.getX() < world.getWidth() / 2
								&& self.getY() > world.getHeight() * 2 / 4) {
							nextY = 2;
							nextX = 2;
						} else if (self.getX() > world.getWidth() * 2 / 4
								&& self.getY() > world.getHeight() * 2 / 4) {
							nextY = world.getHeight() - 2;
							nextX = 2;
						} else if (self.getX() > world.getWidth() * 2 / 4
								&& self.getY() < world.getHeight() * 2 / 4) {
							nextY = world.getHeight() - 2;
							nextX = world.getWidth() - 2;
						}

						if (isDebug)
							System.out.println("nextX =" + nextX + " nextY ="
									+ nextY);
					}

					move.setAction(ActionType.MOVE);

//					if (isDebug && move.getAction() == ActionType.MOVE)
//						System.out.println("-=!=- move fs : " + " X=" + nextX
//								+ " Y=" + nextY + " " + self.getType() + " sx="
//								+ self.getX() + " sy=" + self.getY());
					myMove(nextX, nextY, self, world, move, 0,game);
					showDebug(move,self,isDebug," -!= freeStyle ");
				}
			}
		}
		if ((move.getAction() == ActionType.MOVE && move.getX() == self.getX() && move
				.getY() == self.getY())
				|| move.getAction() == ActionType.END_TURN) {
			move.setAction(ActionType.MOVE);
			// myCommander = (Trooper)
//			if (isDebug && move.getAction() == ActionType.MOVE)
//				System.out.println("-=!=- move fs : " + " X=" + nextX + " Y="
//						+ nextY + " " + self.getType() + " sx=" + self.getX()
//						+ " sy=" + self.getY());
			myMove(nextX, nextY, self, world, move, 0,game);
			showDebug(move,self,isDebug && move.getAction() == ActionType.MOVE," -!= freeStyle ");
		}
		if (move.getAction() == ActionType.MOVE

				&& (self.getActionPoints() < game.getStandingMoveCost()
						|| move.getX() < 0 || move.getY() < 0 || !cellFree(
							move.getX(), move.getY(), world)))
			move.setAction(null);
//		if (move.getAction() == null || move.getAction() == ActionType.END_TURN) {
//			if (isDebug)
//				System.out.println(self.getStance());
//			if (self.getActionPoints() > 1) {
//				if (self.getStance() == TrooperStance.STANDING
//						|| self.getStance() == TrooperStance.KNEELING) {
//					move.setAction(ActionType.LOWER_STANCE);
//					move.setX(self.getX());
//					move.setY(self.getY());
//				}
//			}
//		}
//		if (isDebug)
//			System.out.println("-=!=- " + move.getAction() + " : " + " X="
//					+ move.getX() + " Y=" + move.getY() + " " + self.getType()
//					+ " sx=" + self.getX() + " sy=" + self.getY() + " points="
//					+ self.getActionPoints());
		//showDebug(move,self,isDebug," -!= freeStyle ");
	}
}
