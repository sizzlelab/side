

select from 
	observation_record rec
	LEFT JOIN observation_record_value rv USING(ido_record)
	LEFT JOIN observation_keyname ok USING(ido_keyname)
