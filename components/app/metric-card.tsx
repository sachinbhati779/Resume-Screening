import type { LucideIcon } from "lucide-react";

import { GlassPanel } from "@/components/app/glass-panel";

type MetricCardProps = {
  label: string;
  value: string;
  delta: string;
  icon: LucideIcon;
};

export function MetricCard({ label, value, delta, icon: Icon }: MetricCardProps) {
  return (
    <GlassPanel className="p-5 transition-all duration-200 hover:-translate-y-1 hover:border-[#E85D04]/35">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm text-[#A3A3A3]">{label}</p>
          <p className="mt-3 text-3xl font-semibold text-[#F5F5F5]">{value}</p>
        </div>
        <div className="flex size-10 items-center justify-center rounded-md border border-white/10 bg-white/10 text-[#E85D04]">
          <Icon className="size-5" />
        </div>
      </div>
      <p className="mt-4 text-sm text-[#A3A3A3]">
        <span className="font-medium text-[#E85D04]">{delta}</span> this month
      </p>
    </GlassPanel>
  );
}
