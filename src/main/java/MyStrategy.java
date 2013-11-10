import model.*;

import java.util.Random;

public final class MyStrategy implements Strategy {
	private final Random random = new Random();

	@Override
	public void move(Trooper self, World world, Game game, Move move) {
		if (self.getActionPoints() < 1) {// game.getStandingMoveCost()) {
			return;
		}
		Trooper[] trooreps = world.getTroopers();
		Player[] players = world.getPlayers();
		Bonus[] bonuses = world.getBonuses();

		boolean haveVisibleenemys = false;

		Bonus moveToBonus = null;
		System.out.println("-=Why I am = " + self.getType()+" X="+self.getX()+" Y="+self.getY() + " : "
				+ self.getActionPoints());
		for (int i = 0; i < bonuses.length; i++) {
			// if (players[pid].getId()==trooreps[i].getPlayerId())

			// System.out.println(bonuses[i].getType());
			if (bonuses[i].getType() == bonuses[i].getType().FIELD_RATION
					&& !self.isHoldingFieldRation()) {
				moveToBonus = bonuses[i];
			} else if (bonuses[i].getType() == bonuses[i].getType().GRENADE
					&& !self.isHoldingGrenade()) {
				moveToBonus = bonuses[i];
			} else if (bonuses[i].getType() == bonuses[i].getType().MEDIKIT
					&& !self.isHoldingMedikit()) {
				moveToBonus = bonuses[i];
			}

		}
	//	if (!(moveToBonus == null))
	//		System.out.println("Bonus = " + moveToBonus.getType());

		// System.out.println("	-=Svoy");
		for (int i = 0; i < trooreps.length; i++) {
			// if (players[pid].getId()==trooreps[i].getPlayerId())

			if (trooreps[i].isTeammate() && trooreps[i].getId() != self.getId()) {
				// System.out.println(trooreps[i].getType());
			}

		}

		for (int i = 0; i < trooreps.length; i++) {
			// if (players[pid].getId()==trooreps[i].getPlayerId())

			if (!trooreps[i].isTeammate()) {
				haveVisibleenemys = true;
				// System.out.println(trooreps[i].getType());
			}

		}
		if (haveVisibleenemys)
			System.out.println("	-=Cysie");
		for (int i = 0; i < trooreps.length; i++) {
			// if (players[pid].getId()==trooreps[i].getPlayerId())

			if (!trooreps[i].isTeammate()) {
				// haveVisibleenemys =true;
				System.out.println(trooreps[i].getType());
			}
		}
		if (haveVisibleenemys) {
			move.setAction(ActionType.MOVE);

			if (random.nextBoolean()) {
				move.setDirection(random.nextBoolean() ? Direction.NORTH
						: Direction.SOUTH);
			} else {
				move.setDirection(random.nextBoolean() ? Direction.WEST
						: Direction.EAST);
			}
			// for (int pid = 0; pid < players.length; pid++) {
			// System.out.println("Player: "+players[pid].getName()+" ");
			// for (int i = 0; i < trooreps.length; i++) {
			// if (players[pid].getId()==trooreps[i].getPlayerId())
			// {
			// System.out.println(trooreps[i].getType()+trooreps[i].isTeammate());}
			//
			// }
			// }
		} else // freestyle
		{
			move.setAction(ActionType.MOVE);
			if (!(moveToBonus == null))
			{
				System.out.println("Bonus = " + moveToBonus.getType()+"="+moveToBonus.getX()+" Y="+moveToBonus.getY());
				move.setX(moveToBonus.getX());
				move.setY(moveToBonus.getY());
			
			}
		// get location

		}
	}
}
