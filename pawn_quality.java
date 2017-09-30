
    // private double pawn_quality(int field, int y)
    // {
    // double temp_quality=0;
    //
    // // Bauer: isoliert, doppelt, rueckstaendig, usw
    // if (fields[field]==BLACK_PAWN)
    // {
    // // Bauer n Reihen gelaufen +points_pawn_rows_moved*n
    // temp_quality += -(points_pawn_rows_moved*(8-y));
    //
    // // Bauer in der Mitte des Spielfeldes 2x2 = +2, Rand darum =+1 points_pawn_in_middle=2
    // if (field==54 || field==55 || field==64 || field==65)
    // temp_quality += -points_pawn_in_middle;
    // else if ((field>42 && field<47) || (field>72 && field<77) || field==53 || field==63 ||
    // field==56 || field==66)
    // temp_quality += -(points_pawn_in_middle / 2);
    //
    // // Bauer: doppelt
    // for (int i=1;i<7;i++)
    // {
    // // pro Bauer zuviel in einer Reihe points_pawn_in_a_row=-3
    // if (field-(i*10)>20 && (fields[field-(i*10)]==BLACK_PAWN))
    // {
    // temp_quality += -points_pawn_in_a_row;
    // }
    // }
    //
    // // Bauer isoliert (keine Bauern auf der Nachbarlinie)
    // boolean isolated_pawn = true;
    // for (int i=0;i<7;i++)
    // {
    // // linke und rechte Reihe auf bauer absuchen points_pawn_isolated=-3
    // if ((field+(i*10)+1<99 && (fields[field+(i*10+1)]==BLACK_PAWN ||
    // fields[field+(i*10-1)]==BLACK_PAWN)) || (field-(i*10)-1>20 &&
    // (fields[field-(i*10+1)]==BLACK_PAWN || fields[field-(i*10-1)]==BLACK_PAWN)))
    // isolated_pawn = false;
    //
    // }
    // if (isolated_pawn)
    // temp_quality += -points_pawn_isolated;
    //
    // // Bauer rueckstaendig(ein Bauer mind 2 felder hinter allen anderen) points_pawn_backward=-3
    // boolean backward_pawn = true;
    // for (int i=1;i<7;i++)
    // {
    // // links und rechts auf Nachbarbauer absuchen (2 felder vor ihm)
    // if ((fields[field-i]==BLACK_PAWN || fields[field+i]==BLACK_PAWN ||
    // fields[field-i+10]==BLACK_PAWN || fields[field+i+10]==BLACK_PAWN ||
    // fields[field-i+20]==BLACK_PAWN || fields[field+i+20]==BLACK_PAWN
    // || fields[field-i-10]==BLACK_PAWN || fields[field+i-10]==BLACK_PAWN ||
    // fields[field-i-20]==BLACK_PAWN || fields[field+i-20]==BLACK_PAWN))
    // backward_pawn = false;
    // }
    // if (backward_pawn)
    // temp_quality += -points_pawn_backward;
    // }
    // else
    // {
    // // Bauer n Reihen gelaufen +points_pawn_rows_moved*n
    // temp_quality += (points_pawn_rows_moved*(y-3));
    //
    // // Bauer in der Mitte des Spielfeldes 2x2 = +2, Rand darum =+1 points_pawn_in_middle=2
    // if (field==54 || field==55 || field==64 || field==65)
    // temp_quality += points_pawn_in_middle;
    // else if ((field>42 && field<47) || (field>72 && field<77) || field==53 || field==63 ||
    // field==56 || field==66)
    // temp_quality += points_pawn_in_middle / 2;
    //
    // // Bauer: doppelt
    // for (int i=1;i<7;i++)
    // {
    // // pro Bauer zuviel in einer Reihe points_pawn_in_a_row=-3
    // if (field+(i*10)<99 && (fields[field+(i*10)]==WHITE_PAWN))
    // {
    // temp_quality += points_pawn_in_a_row;
    // }
    // }
    //
    // // Bauer isoliert (keine Bauern auf der Nachbarlinie)
    // boolean isolated_pawn = true;
    // for (int i=0;i<7;i++)
    // {
    // // linke und rechte Reihe auf bauer absuchen points_pawn_isolated=-3
    // if ((field+(i*10)+1<99 && (fields[field+(i*10+1)]==WHITE_PAWN ||
    // fields[field+(i*10-1)]==WHITE_PAWN)) || (field-(i*10)-1>20 &&
    // (fields[field-(i*10+1)]==WHITE_PAWN || fields[field-(i*10-1)]==WHITE_PAWN)))
    // isolated_pawn = false;
    // }
    // if (isolated_pawn)
    // temp_quality += points_pawn_isolated;
    //
    // // Bauer rueckstaendig(ein Bauer mind 2 felder hinter allen anderen) points_pawn_backward=-3
    // boolean backward_pawn = true;
    // for (int i=1;i<7;i++)
    // {
    // // links und rechts auf Nachbarbauer absuchen (2 felder vor ihm)
    // if ((fields[field-i]==WHITE_PAWN || fields[field+i]==WHITE_PAWN ||
    // fields[field-i+10]==WHITE_PAWN || fields[field+i+10]==WHITE_PAWN ||
    // fields[field-i+20]==WHITE_PAWN || fields[field+i+20]==WHITE_PAWN
    // || fields[field-i-10]==WHITE_PAWN || fields[field+i-10]==WHITE_PAWN ||
    // fields[field-i-20]==WHITE_PAWN || fields[field+i-20]==WHITE_PAWN))
    // backward_pawn = false;
    // }
    // if (backward_pawn)
    // temp_quality += points_pawn_backward;
    // }
    // return temp_quality;
    // }
